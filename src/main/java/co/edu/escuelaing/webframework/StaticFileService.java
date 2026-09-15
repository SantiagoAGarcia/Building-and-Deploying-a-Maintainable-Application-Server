package co.edu.escuelaing.webframework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StaticFileService {

    private String staticFilesRoot = "/webroot";

    public void setRoot(String root) {
        if (root == null || root.isBlank()) {
            return;
        }
        this.staticFilesRoot = root.startsWith("/") ? root : "/" + root;
    }

    public byte[] getResourceBytes(String path) {
        String resourcePath = staticFilesRoot + (path.equals("/") ? "/index.html" : path);

        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    public String resolveContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".ico")) return "image/x-icon";
        if (path.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }
}
