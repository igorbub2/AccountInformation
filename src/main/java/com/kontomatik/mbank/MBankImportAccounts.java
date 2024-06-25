package com.kontomatik.mbank;

import com.kontomatik.ImportAccounts;
import com.kontomatik.model.Account;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.stream.Collectors;

class MBankImportAccounts implements ImportAccounts {

  private final MBankHttpRequests requests;
  private final HttpAgent agent;

  MBankImportAccounts(MBankHttpRequests requests, HttpAgent agent) {
    this.requests = requests;
    this.agent = agent;
  }

  @Override
  public List<Account> retrieveAccounts() {
    HttpRequest accountsGroupRequest = requests.prepareRequest("/pl/Accounts/Accounts/AccountsGroups")
      .GET()
      .build();
    MBankAccountsGroups accountsGroups = agent.fetchParsedBody(accountsGroupRequest, MBankAccountsGroups.class).body();
    return mapToAccount(accountsGroups);
  }

  private static List<Account> mapToAccount(MBankAccountsGroups accountsGroups) {
    return accountsGroups.accountsGroups().stream().flatMap(accountGroup -> accountGroup.accounts().stream()
        .map(account -> new Account(account.accountNumber(), account.balance(), account.currency())))
      .collect(Collectors.toList());
  }

  @Override
  public void logout() {
    HttpRequest logoutRequest = requests.prepareRequest("/LoginMain/Account/Logout")
      .GET()
      .build();
    agent.fetchWithoutCorrectResponseAssertion(logoutRequest);
  }

  private record MBankAccount(String accountNumber, String balance, String currency) {
  }

  private record MBankAccountGroup(List<MBankAccount> accounts, String header) {
  }

  private record MBankAccountsGroups(List<MBankAccountGroup> accountsGroups) {
  }

}
