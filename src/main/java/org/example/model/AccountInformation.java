package org.example.model;

public record AccountInformation(String iban, String balance, String currency) {

    @Override
    public String toString() {
        return iban + ": " + balance + " " + currency;
    }
}
