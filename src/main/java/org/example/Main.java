package org.example;

import org.example.client.mbank.MBankClient;
import org.example.model.AccountInformation;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        AccountInformationExtractionService extractionService = new AccountInformationExtractionService(new MBankClient());

        List<AccountInformation> accountInformation = extractionService.getAccountInformation();

        accountInformation.forEach(account -> System.out.println(account.toString()));
    }
}