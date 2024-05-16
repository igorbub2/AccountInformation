package org.example.client.mbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.client.LoggedInBankClient;
import org.example.model.Account;
import org.jsoup.Connection;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class LoggedInMBankClient implements LoggedInBankClient {
    private final Connection connection;
    private final String host;
    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public LoggedInMBankClient(Connection connection, String host) {
        this.connection = connection;
        this.host = host;
    }

    @Override
    public List<Account> retrieveAccountsBalance() {
        Connection.Response accountsGroup;
        try {
            accountsGroup = connection.newRequest(host + "/pl/Accounts/Accounts/AccountsGroups")
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Problem with Accounts groups request: " + e);
        }
        MBankAccountsGroups accountsGroups;
        try {
            accountsGroups = mapper.readValue(accountsGroup.body(), MBankAccountsGroups.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot retrieve accounts groups: " + e);
        }
        return accountsGroups.accountsGroups().stream()
                .flatMap(accountGroup -> accountGroup.accounts().stream()
                        .map(account -> new Account(account.accountNumber(), account.balance(), account.currency())))
                .collect(Collectors.toList());
    }

    @Override
    public void logout() {
        try {
            connection.newRequest(host + "/LoginMain/Account/Logout")
                    .method(Connection.Method.GET)
                    .execute();
        } catch (IOException e) {
            System.out.println("Problem with logout: " + e);
        }
    }
}
