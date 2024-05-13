package org.example.client;

import org.example.model.AccountInformation;

import java.io.IOException;
import java.util.List;

public interface BankClient {

    void login(String login, String password) throws IOException;

    List<AccountInformation> retrieveAccountsBalance() throws IOException;

    void logout() throws IOException;

    Status getStatus();

    enum Status {
        LOGGED_OUT,
        LOGGED_IN
    }
}
