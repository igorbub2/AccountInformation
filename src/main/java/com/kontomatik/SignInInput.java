package com.kontomatik;

import com.kontomatik.mbank.LoginCredentials;

public interface SignInInput {

  LoginCredentials provideCredentials();

  void confirmTwoFactorAuthentication();

}
