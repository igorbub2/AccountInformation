package com.kontomatik.mbank;

import com.kontomatik.ImportAccounts;
import com.kontomatik.model.Account;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.stream.Collectors;

class MBankImportAccounts implements ImportAccounts {

  private final MBankHttpClient httpClient;

  public MBankImportAccounts(MBankHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public List<Account> retrieveAccounts() {
    HttpRequest accountsGroupRequest = httpClient.prepareRequest("/pl/Accounts/Accounts/AccountsGroups")
      .GET()
      .build();
    MBankAccountsGroups accountsGroups = httpClient.fetchParsedBody(accountsGroupRequest, MBankAccountsGroups.class);
    return mapToAccount(accountsGroups);
  }

  private static List<Account> mapToAccount(MBankAccountsGroups accountsGroups) {
    return accountsGroups.accountsGroups().stream().flatMap(accountGroup -> accountGroup.accounts().stream()
        .map(account -> new Account(account.accountNumber(), account.balance(), account.currency())))
      .collect(Collectors.toList());
  }

  @Override
  public void logout() {
    HttpRequest logoutRequest = httpClient.prepareRequest("/LoginMain/Account/Logout")
      .GET()
      .build();
    httpClient.fetchWithoutCorrectResponseAssertion(logoutRequest);
  }

  private record MBankAccount(String accountNumber, String balance, String currency) {
  }

  private record MBankAccountGroup(List<MBankAccount> accounts, String header) {
  }

  private record MBankAccountsGroups(List<MBankAccountGroup> accountsGroups) {
  }

}
