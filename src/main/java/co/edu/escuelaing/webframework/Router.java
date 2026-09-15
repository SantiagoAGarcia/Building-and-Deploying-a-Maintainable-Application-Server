package co.edu.escuelaing.webframework;

import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<String, Service> getRoutes = new HashMap<>();

    public void addGetRoute(String path, Service service) {
        getRoutes.put(path, service);
    }

    public boolean hasGetRoute(String path) {
        return getRoutes.containsKey(path);
    }

    public Service getService(String path) {
        return getRoutes.get(path);
    }
}
