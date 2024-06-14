package org.example;

import org.example.client.exceptions.InvalidCredentials;
import org.example.client.mbank.KeyboardUserInput;
import org.example.client.mbank.MBankAuthentication;
import org.example.model.Account;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        AccountScraper extractionService = new AccountScraper(new MBankAuthentication());
        try {
            List<Account> accountInformation = extractionService.getAccountInformation(new KeyboardUserInput());
            accountInformation.forEach(account -> System.out.println(account.iban() + ": " + account.balance() + " " + account.currency()));
        } catch (InvalidCredentials e) {
            System.out.println(e.getMessage());
        }
    }
}