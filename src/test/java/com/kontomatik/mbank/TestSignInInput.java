package com.kontomatik.mbank;

public record TestSignInInput(LoginAndPassword loginAndPassword, Runnable twoFactorAuthentication) implements SignInInput {

  @Override
  public LoginAndPassword provideLoginAndPassword() {
    return loginAndPassword;
  }

  @Override
  public void confirmTwoFactorAuthentication() {
    twoFactorAuthentication.run();
  }

}
