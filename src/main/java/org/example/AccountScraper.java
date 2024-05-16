package org.example;

import org.example.client.LoggedInBankClient;
import org.example.client.LoggedOutBankClient;
import org.example.model.Account;

import java.util.List;

public class AccountScraper {
    private final LoggedOutBankClient loggedOutBankClient;
    public AccountScraper(LoggedOutBankClient loggedOutBankClient) {
        this.loggedOutBankClient = loggedOutBankClient;
    }
    public List<Account> getAccountInformation(String login, String password) {
        LoggedInBankClient loggedInBankClient = loggedOutBankClient.login(login, password);
        List<Account> account;
        try {
            account = loggedInBankClient.retrieveAccountsBalance();
        } finally {
            loggedInBankClient.logout();
        }
        return account;
    }
}
