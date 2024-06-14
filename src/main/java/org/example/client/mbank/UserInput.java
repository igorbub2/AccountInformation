package org.example.client.mbank;

public interface UserInput {
    void login();

    String getLogin();

    String getPassword();

    void waitForInput();
}
