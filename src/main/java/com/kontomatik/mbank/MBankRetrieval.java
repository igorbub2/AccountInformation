package com.kontomatik.mbank;

import com.kontomatik.Retrieval;
import com.kontomatik.model.Account;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

import static com.kontomatik.mbank.MBankHttpClient.*;

public class MBankRetrieval implements Retrieval {

  private final MBankHttpClient client;

  public MBankRetrieval(MBankHttpClient httpRequest) {
    this.client = httpRequest;
  }

  @Override
  public List<Account> retrieveAccountsBalance() {
    HttpRequest accountsGroupRequest = client.getRequest()
      .uri(buildUri(HOST + "/pl/Accounts/Accounts/AccountsGroups"))
      .GET()
      .build();
    HttpResponse<String> accountsGroup = client.fetchRequest(accountsGroupRequest);
    MBankAccountsGroups accountsGroups = client.parse(accountsGroup.body(), MBankAccountsGroups.class);
    return mapToAccount(accountsGroups);
  }

  private static List<Account> mapToAccount(MBankAccountsGroups accountsGroups) {
    return accountsGroups.accountsGroups().stream().flatMap(accountGroup -> accountGroup.accounts().stream()
        .map(account -> new Account(account.accountNumber(), account.balance(), account.currency())))
      .collect(Collectors.toList());
  }

  @Override
  public void logout() {
    HttpRequest logoutRequest = client.getRequest()
      .uri(buildUri(HOST + "/LoginMain/Account/Logout"))
      .build();
    client.fetchRequestWithoutIncorrectResponseHandling(logoutRequest);
  }

}

record MBankAccount(String accountNumber, String balance, String currency) {
}

record MBankAccountGroup(List<MBankAccount> accounts, String header) {
}

record MBankAccountsGroups(List<MBankAccountGroup> accountsGroups) {
}
