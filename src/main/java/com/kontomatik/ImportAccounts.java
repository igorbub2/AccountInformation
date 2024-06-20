package com.kontomatik;

import com.kontomatik.model.Account;

import java.util.List;

public interface ImportAccounts {

  List<Account> retrieveAccounts();

  void logout();

}
