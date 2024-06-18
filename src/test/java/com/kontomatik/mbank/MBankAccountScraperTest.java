package com.kontomatik.mbank;

import com.kontomatik.AccountScraper;
import com.kontomatik.exceptions.InvalidCredentials;
import com.kontomatik.model.Account;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class MBankAccountScraperTest {

  @Test
  public void shouldFailSignInWithIncorrectCredentials() {
    AccountScraper scraper = new AccountScraper(new MBankAuthentication(new IncorrectCredentialsInput()));
    InvalidCredentials exception = assertThrows(InvalidCredentials.class, scraper::fetchAccounts);
    assertEquals(exception.getMessage(), "Incorrect login credentials");
  }

  @Test
  public void shouldFailTwoFactorAuthentication() {
    AccountScraper scraper = new AccountScraper(new MBankAuthentication((MockSignInInput) () -> {}));
    InvalidCredentials exception = assertThrows(InvalidCredentials.class, scraper::fetchAccounts);
    assertEquals(exception.getMessage(), "Two factor authentication failed");
  }

  @Test
  public void shouldRetrieveAccountsFromBank() {
    AccountScraper scraper = new AccountScraper(new MBankAuthentication((MockSignInInput) () -> {
      try {
        System.out.println("You have 10 seconds to confirm 2FA");
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }));
    List<Account> accounts = scraper.fetchAccounts();
    assertFalse(accounts.isEmpty());
    Set<String> availableCurrencies = Currency.getAvailableCurrencies().stream()
      .map(Currency::getCurrencyCode).collect(Collectors.toSet());
    accounts.forEach(account -> {
        assertTrue(availableCurrencies.contains(account.currency()));
        assertTrue(account.balance().matches("[0-9]*\\.[0-9]{2}"));
        assertTrue(account.iban().matches("[0-9]{2}(?: ?[0-9]{4}){6}"));
      }
    );
  }

}
