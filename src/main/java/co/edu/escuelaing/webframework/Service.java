package co.edu.escuelaing.webframework;

@FunctionalInterface
public interface Service {
    String handle(Request req, Response resp) throws Exception;
}
