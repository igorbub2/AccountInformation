package org.example.client;

public interface LoggedOutBankClient {
    LoggedInBankClient login(String login, String password);
}
