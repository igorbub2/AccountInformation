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

  private final Properties properties;

  public MBankAccountScraperTest() {
    properties = new Properties();
    ExceptionUtils.uncheck(() -> properties.load(getClass().getClassLoader().getResourceAsStream("testLoginCredentials.properties")));
  }

  @Test
  public void shouldFailSignInWithIncorrectCredentials() {
    var scraper = new AccountScraper(new MBankAuthentication(
      new TestSignInInput(
        createIncorrectTestLoginAndPassword(),
        () -> {}
      )
    ));
    InvalidCredentials exception = assertThrows(InvalidCredentials.class, scraper::fetchAccounts);
    assertEquals(exception.getMessage(), "Incorrect login credentials");
  }

  private static LoginAndPassword createIncorrectTestLoginAndPassword() {
    return new LoginAndPassword("testLogin", "testPassword");
  }

  @Test
  public void shouldFailTwoFactorAuthentication() {
    var scraper = new AccountScraper(new MBankAuthentication(
      new TestSignInInput(
        extractTestLoginAndPassword(),
        () -> {}
      )
    ));
    InvalidCredentials exception = assertThrows(InvalidCredentials.class, scraper::fetchAccounts);
    assertEquals(exception.getMessage(), "Two factor authentication failed");
  }

  private LoginAndPassword extractTestLoginAndPassword() {
    String login = properties.getProperty("login");
    String password = properties.getProperty("password");
    if (Strings.isNullOrEmpty(login) || Strings.isNullOrEmpty(password)) {
      throw new RuntimeException();
    }
    return new LoginAndPassword(login, password);
  }

  @Test
  public void shouldRetrieveAccountsFromBank() {
    var scraper = new AccountScraper(new MBankAuthentication(
      new TestSignInInput(
        extractTestLoginAndPassword(),
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
