package com.kontomatik.mbank;

import java.util.function.Supplier;

public record TestSignInInput(Supplier<LoginAndPassword> loginAndPasswordSupplier, Runnable twoFactorAuthentication) implements SignInInput {
  @Override
  public LoginAndPassword provideLoginAndPassword() {
    return loginAndPasswordSupplier.get();
  }

  @Override
  public void confirmTwoFactorAuthentication() {
    twoFactorAuthentication.run();
  }
}
