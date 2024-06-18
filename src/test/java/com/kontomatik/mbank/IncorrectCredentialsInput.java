package com.kontomatik.mbank;

import com.kontomatik.SignInInput;

public class IncorrectCredentialsInput implements SignInInput {

  @Override
  public LoginCredentials provideCredentials() {
    return new LoginCredentials("testLogin", "testPassword");
  }

  @Override
  public void confirmTwoFactorAuthentication() {
  }

}
