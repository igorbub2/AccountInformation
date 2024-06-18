package com.kontomatik;

import com.kontomatik.model.Account;

import java.util.List;

public class AccountScraper {

  private final Authentication authentication;

  public AccountScraper(Authentication authentication) {
    this.authentication = authentication;
  }

  public List<Account> fetchAccounts() {
    Retrieval loggedInBankClient = authentication.signIn();
    try {
      return loggedInBankClient.retrieveAccountsBalance();
    } finally {
      loggedInBankClient.logout();
    }
  }

}
