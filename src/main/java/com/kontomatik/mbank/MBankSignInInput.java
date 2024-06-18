package com.kontomatik.mbank;

import java.io.Console;

public class MBankSignInInput implements SignInInput {

  private final Console console = System.console();

  @Override
  public LoginCredentials provideCredentials() {
    String login = console.readLine("Login (needs to be between 7 and 32 characters):");
    while (login.length() < 7 || login.length() > 32) {
      login = console.readLine("Login incorrect - needs to be between 7 and 32 characters, try again:");
    }
    String password = console.readLine("Password (needs to be between 8 and 32 characters):");
    while (password.length() < 8 || password.length() > 32) {
      password = console.readLine("Password incorrect - needs to be between 8 and 32 characters, try again:");
    }
    return new LoginCredentials(login, password);
  }


  @Override
  public void confirmTwoFactorAuthentication() {
    System.out.println("Press ENTER after confirming two factor authentication");
    console.readLine();
  }

}
