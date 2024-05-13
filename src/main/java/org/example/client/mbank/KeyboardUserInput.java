package org.example.client.mbank;

import java.io.IOException;

public class KeyboardUserInput implements UserInput {
    @Override
    public void waitForInput() throws IOException {
        System.out.println("Press ENTER after confirming two factor authentication");
        System.in.read();
    }
}
