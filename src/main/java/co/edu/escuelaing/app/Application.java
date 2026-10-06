package co.edu.escuelaing.app;

import static co.edu.escuelaing.webframework.WebFramework.*;

/**
 * Example web application demonstrating the Mini Web Framework.
 * Configures static resources, dynamic endpoints (/hello, /pi, /env),
 * environment variables, and the conditional /shutdown route.
 */
public class Application {

    public static void main(String[] args) {
        initApp();
        start();
    }

    public static void initApp() {
        // 1. Configure static resource location
        staticfiles("/webroot");

        // 2. Read environment variables with safe defaults
        String envAppEnv = System.getenv("APP_ENV");
        String appEnv = (envAppEnv != null && !envAppEnv.trim().isEmpty()) ? envAppEnv.trim() : "development";

        String envPrefix = System.getenv("GREETING_PREFIX");
        String greetingPrefix = (envPrefix != null && !envPrefix.trim().isEmpty()) ? envPrefix.trim() : "Hello";

        // 3. Register GET /hello
        get("/hello", (req, resp) -> {
            String name = req.getValue("name");
            String target = (name != null && !name.trim().isEmpty()) ? name.trim() : "world";
            resp.type("text/plain; charset=utf-8");
            return greetingPrefix + " " + target;
        });

        // 4. Register GET /pi
        get("/pi", (req, resp) -> {
            resp.type("text/plain; charset=utf-8");
            return String.valueOf(Math.PI);
        });

        // 5. Register GET /env (exposes non-sensitive cloud environment info)
        get("/env", (req, resp) -> {
            resp.type("application/json; charset=utf-8");
            return String.format(
                    "{\"appEnv\":\"%s\",\"greetingPrefix\":\"%s\",\"port\":%d}",
                    escapeJson(appEnv),
                    escapeJson(greetingPrefix),
                    resolvePort()
            );
        });

        // 6. Conditionally register GET /shutdown only in development
        if (appEnv.equals("development")) {
            System.out.println("[Application] Environment is 'development'. Registering /shutdown endpoint.");
            get("/shutdown", (req, resp) -> {
                System.out.println("[Application] /shutdown invoked. Stopping server gracefully...");
                stop();
                resp.type("text/plain; charset=utf-8");
                return "Server is shutting down gracefully.";
            });
        } else {
            System.out.println("[Application] Environment is '" + appEnv + "'. /shutdown endpoint is DISABLED.");
        }
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
