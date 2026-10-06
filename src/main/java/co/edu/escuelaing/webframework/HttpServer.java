package co.edu.escuelaing.webframework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Sequential HTTP/1.1 Server.
 * Runs on a single thread with a single connection accept loop.
 * Guarantees that each connection is completely served and closed before accepting the next.
 */
public class HttpServer {

    private final Router router;
    private final StaticFileService staticFileService;
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private int boundPort = 8080;

    public HttpServer(Router router, StaticFileService staticFileService) {
        this.router = router;
        this.staticFileService = staticFileService;
    }

    /**
     * Starts listening on the specified port. Binds to all network interfaces (0.0.0.0).
     *
     * @param port the TCP port to listen on
     */
    public void start(int port) {
        this.boundPort = port;
        this.running = true;

        System.out.println("===============================================================");
        System.out.println("  Mini Web Framework HTTP Server");
        System.out.println("  Port: " + port + " | Bound to: 0.0.0.0 (All network interfaces)");
        System.out.println("  Concurrency: NONE (Strictly Sequential)");
        System.out.println("===============================================================");

        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
            this.boundPort = serverSocket.getLocalPort();
            System.out.println("Server is listening on port " + this.boundPort + ". Ready for requests.");

            while (running) {
                try {
                    // Sequential accept loop: one client at a time
                    Socket clientSocket = serverSocket.accept();
                    try (clientSocket) {
                        clientSocket.setSoTimeout(5000); // Protect against slow/hanging clients
                        handleConnection(clientSocket);
                    }
                } catch (SocketTimeoutException ignored) {
                    // Client read timeout
                } catch (IOException e) {
                    if (!running) {
                        break;
                    }
                    System.err.println("Connection error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Fatal server socket error: " + e.getMessage());
            }
        } finally {
            closeServerSocket();
            System.out.println("Server stopped gracefully.");
        }
    }

    /**
     * Stops the sequential server loop.
     */
    public void stop() {
        this.running = false;
        // Do not forcibly terminate the socket immediately here if we are currently handling /shutdown
        // Let the current request complete its response, then close
    }

    /**
     * Forcibly closes the listening server socket if needed (e.g. from background thread during tests).
     */
    public synchronized void forceStop() {
        this.running = false;
        closeServerSocket();
    }

    private synchronized void closeServerSocket() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void handleConnection(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            OutputStream out = clientSocket.getOutputStream();

            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.trim().isEmpty()) {
                return;
            }

            String[] parts = requestLine.trim().split("\\s+");
            if (parts.length < 2) {
                Response badReq = new Response().status(400, "Bad Request")
                        .type("text/plain; charset=utf-8")
                        .body("400 Bad Request: Malformed HTTP request line");
                badReq.writeTo(out);
                return;
            }

            String method = parts[0].toUpperCase();
            String fullUri = parts[1];

            // Read request headers
            Map<String, String> headers = new HashMap<>();
            String headerLine;
            while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
                int colonIdx = headerLine.indexOf(':');
                if (colonIdx > 0) {
                    String hName = headerLine.substring(0, colonIdx).trim().toLowerCase();
                    String hVal = headerLine.substring(colonIdx + 1).trim();
                    headers.put(hName, hVal);
                }
            }

            Request req = new Request(method, fullUri, headers);
            System.out.printf("[%s] %s%n", req.getMethod(), req.getPath());

            // 1. Method check: Support only GET for this framework specification
            if (!"GET".equalsIgnoreCase(method)) {
                Response methodNotAllowed = new Response().status(405, "Method Not Allowed")
                        .type("text/plain; charset=utf-8")
                        .header("Allow", "GET")
                        .body("405 Method Not Allowed");
                methodNotAllowed.writeTo(out);
                return;
            }

            // 2. Dynamic route lookup
            RouteHandler handler = router.getHandler(method, req.getPath());
            if (handler != null) {
                Response resp = new Response();
                try {
                    String result = handler.handle(req, resp);
                    if (result != null && (resp.getBody() == null || resp.getBody().length == 0)) {
                        resp.body(result);
                    }
                    resp.writeTo(out);
                } catch (Exception e) {
                    System.err.println("Handler exception on path " + req.getPath() + ": " + e.getMessage());
                    Response errResp = new Response().status(500, "Internal Server Error")
                            .type("text/plain; charset=utf-8")
                            .body("500 Internal Server Error: " + (e.getMessage() != null ? e.getMessage() : "Handler error"));
                    errResp.writeTo(out);
                }
                return;
            }

            // 3. Static file lookup
            try {
                StaticFileService.StaticResource staticFile = staticFileService.resolve(req.getPath());
                if (staticFile != null) {
                    Response resp = new Response().status(200, "OK")
                            .type(staticFile.getContentType())
                            .body(staticFile.getBytes());
                    resp.writeTo(out);
                    return;
                }
            } catch (SecurityException se) {
                // Path traversal detected
                Response forbidden = new Response().status(400, "Bad Request")
                        .type("text/plain; charset=utf-8")
                        .body("400 Bad Request: " + se.getMessage());
                forbidden.writeTo(out);
                return;
            }

            // 4. Neither dynamic route nor static file found -> 404
            Response notFound = new Response().status(404, "Not Found")
                    .type("text/plain; charset=utf-8")
                    .body("404 Not Found");
            notFound.writeTo(out);

        } catch (IOException e) {
            System.err.println("I/O error during request handling: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getBoundPort() {
        return boundPort;
    }
}
