package co.edu.escuelaing.webframework;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maps HTTP method and normalized path pairs to RouteHandler instances.
 */
public class Router {

    private final Map<String, RouteHandler> routes = new HashMap<>();

    public synchronized void addRoute(String method, String path, RouteHandler handler) {
        if (method == null || path == null || handler == null) {
            throw new IllegalArgumentException("Method, path, and handler must not be null");
        }
        String key = routeKey(method, path);
        routes.put(key, handler);
    }

    public synchronized RouteHandler getHandler(String method, String path) {
        if (method == null || path == null) {
            return null;
        }
        return routes.get(routeKey(method, path));
    }

    public synchronized boolean hasRoute(String method, String path) {
        return getHandler(method, path) != null;
    }

    public synchronized boolean hasPathAnyMethod(String path) {
        String normalized = normalizePath(path);
        for (String key : routes.keySet()) {
            if (key.endsWith(" " + normalized)) {
                return true;
            }
        }
        return false;
    }

    public synchronized Set<String> getRegisteredKeys() {
        return Collections.unmodifiableSet(routes.keySet());
    }

    public synchronized void clear() {
        routes.clear();
    }

    private static String routeKey(String method, String path) {
        return method.trim().toUpperCase() + " " + normalizePath(path);
    }

    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        String clean = path.trim();
        return clean.startsWith("/") ? clean : "/" + clean;
    }
}
