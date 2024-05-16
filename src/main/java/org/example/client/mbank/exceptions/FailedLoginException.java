package org.example.client.mbank.exceptions;

public class FailedLoginException extends RuntimeException {
    private final int statusCode;
    public FailedLoginException(int statusCode) {
        super("Login failed, status code: " + statusCode);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
