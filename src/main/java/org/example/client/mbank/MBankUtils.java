package org.example.client.mbank;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;

public class MBankUtils {
    static HttpResponse<String> executeRequest(HttpRequest request, HttpClient client) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static URI buildUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static void handleIncorrectResponse(int statusCode) {
        if (statusCode < 200 || statusCode >=300) {
            throw new RuntimeException();
        }
    }

    static <T> T run(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
