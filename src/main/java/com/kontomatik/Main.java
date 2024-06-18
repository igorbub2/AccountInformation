package com.kontomatik;

import com.kontomatik.exceptions.InvalidCredentials;
import com.kontomatik.mbank.MBankAuthentication;
import com.kontomatik.mbank.MBankSignInInput;
import com.kontomatik.model.Account;

import java.util.List;

public class Main {

  public static void main(String[] args) {
    AccountScraper extractionService = new AccountScraper(new MBankAuthentication(new MBankSignInInput()));
    try {
      List<Account> accountInformation = extractionService.fetchAccounts();
      accountInformation.forEach(account -> System.out.println(account.iban() + ": " + account.balance() + " " + account.currency()));
    } catch (InvalidCredentials e) {
      System.out.println(e.getMessage());
    }
  }

}