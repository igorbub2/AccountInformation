package org.example;

import org.example.client.Retrieval;
import org.example.client.Authentication;
import org.example.client.mbank.UserInput;
import org.example.model.Account;

import java.util.List;

public class AccountScraper {
    private final Authentication authentication;
    public AccountScraper(Authentication authentication) {
        this.authentication = authentication;
    }
    public List<Account> getAccountInformation(UserInput userInput) {
        Retrieval loggedInBankClient = authentication.login(userInput);
        try {
            return loggedInBankClient.retrieveAccountsBalance();
        } finally {
            loggedInBankClient.logout();
        }
    }
}
