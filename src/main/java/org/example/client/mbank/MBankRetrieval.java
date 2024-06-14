package org.example.client.mbank;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.client.Retrieval;
import org.example.model.Account;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.client.mbank.MBankUtils.*;

public class MBankRetrieval implements Retrieval {
    private final HttpClient client;

    private final HttpRequest.Builder request;

    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public MBankRetrieval(HttpRequest.Builder request, HttpClient client) {
        this.request = request;
        this.client = client;
    }

    @Override
    public List<Account> retrieveAccountsBalance() {
        HttpRequest accountsGroupRequest = request.copy()
                .uri(buildUri("https://online.mbank.pl/pl/Accounts/Accounts/AccountsGroups"))
                .GET()
                .build();
        HttpResponse<String> accountsGroup = executeRequest(accountsGroupRequest, client);
        handleIncorrectResponse(accountsGroup.statusCode());
        MBankAccountsGroups accountsGroups = run(() -> mapper.readValue(accountsGroup.body(), MBankAccountsGroups.class));
        return accountsGroups.accountsGroups().stream().flatMap(accountGroup -> accountGroup.accounts().stream()
                        .map(account -> new Account(account.accountNumber(), account.balance(), account.currency())))
                .collect(Collectors.toList());
    }

    @Override
    public void logout() {
        HttpRequest logoutRequest = request.copy()
                .uri(buildUri("https://online.mbank.pl/LoginMain/Account/Logout"))
                .build();
        executeRequest(logoutRequest, client);
    }
}

record MBankAccount(String accountNumber, String balance, String currency) {
}

record MBankAccountGroup(List<MBankAccount> accounts, String header) {
}

record MBankAccountsGroups(List<MBankAccountGroup> accountsGroups) {
}
