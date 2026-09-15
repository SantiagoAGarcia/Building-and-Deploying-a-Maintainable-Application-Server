package co.edu.escuelaing.webframework;

public class Response {

    private int statusCode = 200;
    private String contentType = "text/plain";

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
