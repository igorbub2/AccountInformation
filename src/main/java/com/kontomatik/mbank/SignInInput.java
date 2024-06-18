package com.kontomatik.mbank;

public interface SignInInput {

  LoginCredentials provideCredentials();

  void confirmTwoFactorAuthentication();

}
