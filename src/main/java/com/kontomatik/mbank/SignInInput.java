package com.kontomatik.mbank;

public interface SignInInput {

  LoginAndPassword provideLoginAndPassword();

  void confirmTwoFactorAuthentication();

}
