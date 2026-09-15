package co.edu.escuelaing.webframework;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private final String method;
    private final String path;
    private final Map<String, String> queryParams = new HashMap<>();

    public Request(String method, String path, String rawQuery) {
        this.method = method;
        this.path = path;
        parseQuery(rawQuery);
    }

    private void parseQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return;
        }
        for (String pair : rawQuery.split("&")) {
            if (pair.isBlank()) {
                continue;
            }
            String[] kv = pair.split("=", 2);
            String key = decode(kv[0]);
            String value = kv.length > 1 ? decode(kv[1]) : "";
            queryParams.put(key, value);
        }
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    public String getValue(String key) {
        return queryParams.get(key);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}
