package org.example.client.mbank;

import java.util.List;

public record MBankAccountGroup(List<MBankAccount> accounts, String header) {
}
