package co.edu.escuelaing.webframework;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestTest {

    @Test
    void shouldReturnSingleQueryParam() {
        Request request = new Request("GET", "/hello", "name=Pedro");
        assertEquals("Pedro", request.getValue("name"));
    }

    @Test
    void shouldReturnMultipleQueryParams() {
        Request request = new Request("GET", "/hello", "name=Pedro&language=en");
        assertEquals("Pedro", request.getValue("name"));
        assertEquals("en", request.getValue("language"));
    }

    @Test
    void shouldReturnNullForMissingParam() {
        Request request = new Request("GET", "/hello", "name=Pedro");
        assertNull(request.getValue("language"));
    }

    @Test
    void shouldHandleNullQueryString() {
        Request request = new Request("GET", "/pi", null);
        assertNull(request.getValue("anything"));
    }
}
