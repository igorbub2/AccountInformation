package org.example.client.mbank.exceptions;

public class FailedTwoFactorAuthenticationException extends RuntimeException {
    private final int statusCode;
    public FailedTwoFactorAuthenticationException(int statusCode) {
        super("Two factor authentication failed, status code: " + statusCode);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
