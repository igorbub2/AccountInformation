package org.example;

import org.example.client.BankClient;
import org.example.model.AccountInformation;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import static org.example.client.BankClient.Status.LOGGED_OUT;

public class AccountInformationExtractionService {

    private final BankClient client;

    public AccountInformationExtractionService(BankClient client) {
        this.client = client;
    }

    public List<AccountInformation> getAccountInformation() {
        login();
        List<AccountInformation> accountInformation;
        try {
            accountInformation = client.retrieveAccountsBalance();
        } catch (IOException e) {
            throw new RuntimeException("Problem with retrieving account information");
        } finally {
            logout();
        }
        return accountInformation;
    }

    private void login() {
        Scanner scanner = new Scanner(System.in);
        while (client.getStatus().equals(LOGGED_OUT)) {
            System.out.println("Login:");
            String login = scanner.nextLine();
            System.out.println("Password:");
            String password = scanner.nextLine();
            try {
                client.login(login, password);
            } catch (Exception e) {
                System.out.println("Unsuccessful login");
                System.out.println(e.getMessage());
            }
        }
    }

    private void logout() {
        try {
            client.logout();
        } catch (IOException e) {
            System.out.println("Problem with logout: " + e.getMessage());
        }
    }
}
