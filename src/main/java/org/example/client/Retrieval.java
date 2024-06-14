package org.example.client;

import org.example.model.Account;

import java.util.List;

public interface Retrieval {
    List<Account> retrieveAccountsBalance();

    void logout();
}
