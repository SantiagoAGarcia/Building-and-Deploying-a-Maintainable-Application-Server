package co.edu.escuelaing.webframework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpServer {

    private static volatile boolean running = false;

    private HttpServer() {
    }

    public static void start(int port, Router router, StaticFileService staticFileService) throws IOException {
        running = true;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (running) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleRequest(clientSocket, router, staticFileService);
                } catch (IOException e) {
                    System.out.println("Error handling a request: " + e.getMessage());
                }
            }
        }

        System.out.println("Server stopped gracefully.");
    }

    public static void stop() {
        running = false;
    }

    private static void handleRequest(Socket clientSocket, Router router, StaticFileService staticFileService)
            throws IOException {

        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
        OutputStream out = clientSocket.getOutputStream();

        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isBlank()) {
            writeResponse(out, 400, "text/plain", "400 Bad Request".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String header;
        while ((header = in.readLine()) != null && !header.isBlank()) {
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            writeResponse(out, 400, "text/plain", "400 Bad Request".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String method = parts[0];
        String fullPath = parts[1];
        String path = fullPath;
        String rawQuery = null;

        int qIndex = fullPath.indexOf('?');
        if (qIndex >= 0) {
            path = fullPath.substring(0, qIndex);
            rawQuery = fullPath.substring(qIndex + 1);
        }

        if (!"GET".equalsIgnoreCase(method)) {
            writeResponse(out, 405, "text/plain", "405 Method Not Allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }

        Request request = new Request(method, path, rawQuery);
        Response response = new Response();

        if (router.hasGetRoute(path)) {
            handleDynamicRoute(router, request, response, out);
            return;
        }

        handleStaticResource(staticFileService, path, out);
    }

    private static void handleDynamicRoute(Router router, Request request, Response response, OutputStream out)
            throws IOException {
        try {
            String body = router.getService(request.getPath()).handle(request, response);
            byte[] bodyBytes = (body == null ? "" : body).getBytes(StandardCharsets.UTF_8);
            writeResponse(out, response.getStatusCode(), response.getContentType(), bodyBytes);
        } catch (Exception e) {
            String message = "500 Internal Server Error: " + e.getMessage();
            writeResponse(out, 500, "text/plain", message.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void handleStaticResource(StaticFileService staticFileService, String path, OutputStream out)
            throws IOException {
        byte[] resourceBytes = staticFileService.getResourceBytes(path);

        if (resourceBytes == null) {
            writeResponse(out, 404, "text/plain", "404 Not Found".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String contentType = staticFileService.resolveContentType(
                path.equals("/") ? "/index.html" : path);
        writeResponse(out, 200, contentType, resourceBytes);
    }

    private static void writeResponse(OutputStream out, int statusCode, String contentType, byte[] body)
            throws IOException {
        String statusText = switch (statusCode) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "";
        };

        StringBuilder headers = new StringBuilder();
        headers.append("HTTP/1.1 ").append(statusCode).append(' ').append(statusText).append("\r\n");
        headers.append("Content-Type: ").append(contentType).append("\r\n");
        headers.append("Content-Length: ").append(body.length).append("\r\n");
        headers.append("Connection: close\r\n");
        headers.append("\r\n");

        out.write(headers.toString().getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }
}
