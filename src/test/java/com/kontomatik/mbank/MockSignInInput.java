package com.kontomatik.mbank;

import com.kontomatik.SignInInput;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public interface MockSignInInput extends SignInInput {

  @Override
  default LoginCredentials provideCredentials() {
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream("src/test/resources/testLoginCredentials.properties"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String login = properties.getProperty("login");
    String password = properties.getProperty("password");

    return new LoginCredentials(login, password);
  }

}
