package org.example.client.mbank;

import java.io.Console;

public class KeyboardUserInput implements UserInput {
    private final Console console = System.console();
    private String login;
    private String password;

    @Override
    public void login() {
        login = console.readLine("Login (needs to be between 7 and 32 characters):");
        while (login.length() < 7 || login.length() > 32) {
            login = console.readLine("Login incorrect - needs to be between 7 and 32 characters, try again:");
        }
        password = console.readLine("Password (needs to be between 8 and 32 characters):");
        while (password.length() < 8 || password.length() > 32) {
            password = console.readLine("Password incorrect - needs to be between 8 and 32 characters, try again:");
        }
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String getPassword() {
        return password;
    }


    @Override
    public void waitForInput() {
        System.out.println("Press ENTER after confirming two factor authentication");
        console.readLine();
    }
}
