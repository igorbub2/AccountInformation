package com.kontomatik;

import com.kontomatik.mbank.*;
import com.kontomatik.model.Account;

import java.util.List;

public class Main {

  public static void main(String[] args) {
    var accountScraper = new AccountScraper(new MBankAuthentication(new MBankSignInInput()));
    List<Account> accounts = accountScraper.fetchAccounts();
    accounts.forEach(account -> System.out.println(account.iban() + ": " + account.balance() + " " + account.currency()));
  }

}