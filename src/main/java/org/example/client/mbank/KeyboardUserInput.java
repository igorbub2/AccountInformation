package org.example.client.mbank;

import java.io.IOException;

public class KeyboardUserInput implements UserInput {
    @Override
    public void waitForInput() {
        System.out.println("Press ENTER after confirming two factor authentication");
        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException("Problem with user input");
        }
    }
}
