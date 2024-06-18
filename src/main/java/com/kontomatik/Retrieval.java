package com.kontomatik;

import com.kontomatik.model.Account;

import java.util.List;

public interface Retrieval {

  List<Account> retrieveAccountsBalance();

  void logout();

}
