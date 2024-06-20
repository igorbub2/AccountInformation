package com.kontomatik.mbank;

import com.kontomatik.AccountScraper;
import com.kontomatik.exceptions.ExceptionUtils;
import com.kontomatik.exceptions.InvalidCredentials;
import com.kontomatik.model.Account;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class MBankAccountScraperTest {

  @Test
  public void shouldFailSignInWithIncorrectCredentials() {
    AccountScraper scraper = new AccountScraper(new MBankAuthentication(
      new TestSignInInput(
        MBankAccountScraperTest::createIncorrectTestLoginAndPassword,
        () -> {}
      )
    ));
    InvalidCredentials exception = assertThrows(InvalidCredentials.class, scraper::fetchAccounts);
    assertEquals(exception.getMessage(), "Incorrect login credentials");
  }

  @Test
  public void shouldFailTwoFactorAuthentication() {
    AccountScraper scraper = new AccountScraper(new MBankAuthentication(
      new TestSignInInput(
        this::extractTestLoginAndPassword,
        () -> {}
      )
    ));
    InvalidCredentials exception = assertThrows(InvalidCredentials.class, scraper::fetchAccounts);
    assertEquals(exception.getMessage(), "Two factor authentication failed");
  }

  @Test
  public void shouldRetrieveAccountsFromBank() {
    AccountScraper scraper = new AccountScraper(new MBankAuthentication(
      new TestSignInInput(
        this::extractTestLoginAndPassword,
        MBankAccountScraperTest::waitForUserAction
      )
    ));
    List<Account> accounts = scraper.fetchAccounts();
    assertFalse(accounts.isEmpty());
    accounts.forEach(account -> {
        assertEquals("PLN", account.currency());
        assertTrue(account.balance().matches("[0-9]*\\.[0-9]{2}"));
        assertTrue(account.iban().matches("[0-9]{2}(?: ?[0-9]{4}){6}"));
      }
    );
  }

  private LoginAndPassword extractTestLoginAndPassword() {
    Properties properties = new Properties();
    ExceptionUtils.uncheck(() -> properties.load(getClass().getClassLoader().getResourceAsStream("testLoginCredentials.properties")));
    String login = properties.getProperty("login");
    String password = properties.getProperty("password");
    return new LoginAndPassword(login, password);
  }

  private static void waitForUserAction() {
    System.out.println("You have 10 seconds to confirm 2FA");
    ExceptionUtils.uncheck(() -> Thread.sleep(10000));
  }

  private static LoginAndPassword createIncorrectTestLoginAndPassword() {
    return new LoginAndPassword("testLogin", "testPassword");
  }

}
