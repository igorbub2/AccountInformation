package com.kontomatik.mbank;

import com.google.common.base.Strings;
import com.kontomatik.AccountScraper;
import com.kontomatik.exceptions.ExceptionUtils;
import com.kontomatik.exceptions.InvalidCredentials;
import com.kontomatik.model.Account;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class MBankAccountScraperTest {

  private static final LoginAndPassword VALID_LOGIN_AND_PASSWORD = loadLoginAndPasswordFromProperties();

  private static LoginAndPassword loadLoginAndPasswordFromProperties() {
    var properties = new Properties();
    ExceptionUtils.uncheck(() -> properties.load(MBankAccountScraperTest.class.getResourceAsStream("testLoginCredentials.properties")));
    String login = properties.getProperty("login");
    String password = properties.getProperty("password");
    if (Strings.isNullOrEmpty(login) || Strings.isNullOrEmpty(password))
      throw new RuntimeException();
    return new LoginAndPassword(login, password);
  }

  @Test
  public void shouldFailSignInWithIncorrectCredentials() {
    var scraper = new AccountScraper(new MBankAuthentication(
      new TestSignInInput(
        new LoginAndPassword("testLogin", "testPassword"),
        () -> {}
      )
    ));
    InvalidCredentials exception = assertThrows(InvalidCredentials.class, scraper::fetchAccounts);
    assertEquals(exception.getMessage(), "Incorrect login credentials");
  }

  @Test
  public void shouldFailTwoFactorAuthentication() {
    var scraper = new AccountScraper(new MBankAuthentication(
      new TestSignInInput(
        VALID_LOGIN_AND_PASSWORD,
        () -> {}
      )
    ));
    InvalidCredentials exception = assertThrows(InvalidCredentials.class, scraper::fetchAccounts);
    assertEquals(exception.getMessage(), "Two factor authentication failed");
  }

  @Test
  public void shouldRetrieveAccountsFromBank() {
    var scraper = new AccountScraper(new MBankAuthentication(
      new TestSignInInput(
        VALID_LOGIN_AND_PASSWORD,
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

  private static void waitForUserAction() {
    System.out.println("You have 10 seconds to confirm 2FA");
    ExceptionUtils.uncheck(() -> Thread.sleep(10000));
  }

}
