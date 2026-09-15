package co.edu.escuelaing.webframework;

import java.io.IOException;

public class WebFramework {

    private static final Router router = new Router();
    private static final StaticFileService staticFileService = new StaticFileService();

    private WebFramework() {
    }

    public static void staticfiles(String root) {
        staticFileService.setRoot(root);
    }

    public static void get(String path, Service service) {
        router.addGetRoute(path, service);
    }

    public static void start() throws IOException {
        String portValue = System.getenv("PORT");
        int port = (portValue == null || portValue.isBlank())
                ? 8080
                : Integer.parseInt(portValue);
        start(port);
    }

    public static void start(int port) throws IOException {
        HttpServer.start(port, router, staticFileService);
    }

    public static void stop() {
        HttpServer.stop();
    }
}
