package com.kontomatik;

import com.kontomatik.model.Account;

import java.util.List;

public class AccountScraper {

  private final Authentication authentication;

  public AccountScraper(Authentication authentication) {
    this.authentication = authentication;
  }

  public List<Account> fetchAccounts() {
    ImportAccounts importAccounts = authentication.signIn();
    try {
      return importAccounts.retrieveAccounts();
    } finally {
      importAccounts.logout();
    }
  }

}
