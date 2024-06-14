package org.example.client.mbank;

import org.example.AccountScraper;
import org.example.model.Account;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class MBankAccountScraperTest {

    @Test
    public void shouldRetrieveAccountsFromBank() {
        AccountScraper scraper = new AccountScraper(new MBankAuthentication());
        List<Account> accounts = scraper.getAccountInformation(new MockUserInput());
        assertFalse(accounts.isEmpty());
    }

    private static class MockUserInput implements UserInput {
        private String login;
        private String password;

        @Override
        public void login() {
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream("src/test/resources/testLoginCredentials.properties"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            login = properties.getProperty("login");
            password = properties.getProperty("password");
        }

        @Override
        public String getLogin() {
            return login;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public void waitForInput() {
            try {
                System.out.println("You have 10 seconds to confirm 2FA");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
