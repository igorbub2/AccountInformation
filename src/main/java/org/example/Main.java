package org.example;

import org.example.client.exceptions.InvalidCredentialsException;
import org.example.client.mbank.KeyboardUserInput;
import org.example.client.mbank.LoggedOutMBankClient;
import org.example.model.Account;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Login:");
        String login = scanner.nextLine();
        System.out.println("Password:");
        String password = scanner.nextLine();
        AccountScraper extractionService = new AccountScraper(new LoggedOutMBankClient(new KeyboardUserInput(), "https://online.mbank.pl"));
        try {
            List<Account> accountInformation = extractionService.getAccountInformation(login, password);
            accountInformation.forEach(account -> System.out.println(account.iban() + ": " + account.balance() + " " + account.currency()));
        } catch (InvalidCredentialsException e) {
            System.out.println(e.getMessage());
        }
    }
}