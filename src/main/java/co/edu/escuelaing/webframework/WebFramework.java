package co.edu.escuelaing.webframework;

/**
 * Static façade API for the Mini Web Framework.
 * Provides DSL methods for registering static assets, dynamic GET routes,
 * starting, and stopping the HTTP application server.
 */
public class WebFramework {

    private static final Router router = new Router();
    private static final StaticFileService staticFileService = new StaticFileService();
    private static HttpServer server = new HttpServer(router, staticFileService);

    private WebFramework() {
        // Static utility class
    }

    /**
     * Configures the static file resource folder (default "/webroot").
     *
     * @param folder classpath folder path
     */
    public static void staticfiles(String folder) {
        staticFileService.setStaticFolder(folder);
    }

    /**
     * Registers a GET route handler for the specified path.
     *
     * @param path    the route path (e.g. "/hello")
     * @param handler the functional route handler lambda
     */
    public static void get(String path, RouteHandler handler) {
        router.addRoute("GET", path, handler);
    }

    /**
     * Starts the sequential HTTP server using the port configured in the
     * PORT environment variable (default: 8080).
     */
    public static void start() {
        start(resolvePort());
    }

    /**
     * Starts the sequential HTTP server on the given port.
     *
     * @param port the TCP port to listen on
     */
    public static void start(int port) {
        server = new HttpServer(router, staticFileService);
        server.start(port);
    }

    /**
     * Marks the running server loop as stopped.
     */
    public static void stop() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Forces immediate stop (used in unit test teardowns).
     */
    public static void forceStop() {
        if (server != null) {
            server.forceStop();
        }
    }

    /**
     * Clears registered routes and resets framework state (useful for tests).
     */
    public static void reset() {
        if (server != null) {
            server.forceStop();
        }
        router.clear();
        staticFileService.setStaticFolder("/webroot");
    }

    public static HttpServer getServer() {
        return server;
    }

    public static Router getRouter() {
        return router;
    }

    public static StaticFileService getStaticFileService() {
        return staticFileService;
    }

    /**
     * Resolves the server port from the PORT environment variable.
     * If invalid or missing, logs a message and falls back to 8080.
     *
     * @return the resolved integer port
     */
    public static int resolvePort() {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                int p = Integer.parseInt(envPort.trim());
                if (p > 0 && p <= 65535) {
                    return p;
                }
                System.err.printf("Notice: PORT environment variable '%s' is out of range (1-65535). Falling back to default port 8080.%n", envPort);
            } catch (NumberFormatException e) {
                System.err.printf("Notice: PORT environment variable '%s' is not a valid integer. Falling back to default port 8080.%n", envPort);
            }
        }
        return 8080;
    }
}
