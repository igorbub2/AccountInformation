package org.example.client.mbank;

import org.example.client.exceptions.InvalidCredentialsException;
import org.example.client.LoggedInBankClient;
import org.example.client.LoggedOutBankClient;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoggedOutMBankClient implements LoggedOutBankClient {
    private final UserInput userInput;
    private final String host;
    private final Connection connection = Jsoup.newSession();
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_TYPE_VALUE = "application/json";

    public LoggedOutMBankClient(UserInput userInput, String host) {
        this.userInput = userInput;
        this.host = host;
    }

    @Override
    public LoggedInBankClient login(String login, String password) {
        connection.header("X-Requested-With", "XMLHttpRequest");
        validateLoginCredentials(login, password);
        login(connection, login, password);
        initializeTwoFactorAuthentication(connection);
        finalizeAuthorization(connection);
        return new LoggedInMBankClient(connection, host);
    }

    private void validateLoginCredentials(String login, String password) {
        List<String> loginCredentialsExceptionMessages = new ArrayList<>();
        if (login.length() < 7 || login.length() > 24) {
            loginCredentialsExceptionMessages.add("Login needs to be between 7 and 24 characters");
        }
        if (password.length() < 8 || password.length() > 32) {
            loginCredentialsExceptionMessages.add("Password needs to be between 8 and 32 characters");
        }
        if (!loginCredentialsExceptionMessages.isEmpty()) {
            throw new InvalidCredentialsException(String.join("\n", loginCredentialsExceptionMessages));
        }
    }

    private void login(Connection connection, String login, String password) {
        try {
            Connection.Response loginResponse = connection.newRequest(host + "/pl/LoginMain/Account/JsonLogin")
                    .requestBody(getLoginRequestBody(login, password))
                    .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute();
            connection.header("X-Tab-Id", loginResponse.cookies().get("mBank_tabId"));
        } catch (IOException e) {
            if (e instanceof HttpStatusException && ((HttpStatusException)e).getStatusCode() == 519) {
                throw new InvalidCredentialsException("Incorrect login credentials");
            }
            throw new RuntimeException("Problem with login request: " + e);
        }
    }

    private static String getLoginRequestBody(String login, String password) {
        String requestBody = """
                {
                  "UserName": "%s",
                  "Password": "%s",
                  "Seed": "",
                  "Scenario": "Default",
                  "UWAdditionalParams": {
                    "InOut": null,
                    "ReturnAddress": "",
                    "Source": null
                  },
                  "Lang": "",
                  "HrefHasHash": false,
                  "DfpData": {
                    "dfp": "",
                    "errorMessage": null
                  }
                }
                """;
        return String.format(requestBody, login, password);
    }

    private void initializeTwoFactorAuthentication(Connection connection) {
        try {
            connection.newRequest(host + "/api/authorization/initialize")
                    .method(Connection.Method.POST)
                    .requestBody(getAuthorizationBody())
                    .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Problem with Initialize two factor authentication request: " + e);
        }
    }

    private static String getAuthorizationBody() {
        return """
                {
                  "Url": "sca/authorization/disposable/hostless",
                  "Method": "POST",
                  "Data": {
                    "ScaAuthorizationId": ""
                  }
                }
                """;
    }

    private void finalizeAuthorization(Connection connection) {
        userInput.waitForInput();
        try {
            connection.newRequest(host + "/pl/Sca/FinalizeAuthorization")
                    .method(Connection.Method.POST)
                    .requestBody(getFinalizeAuthorizationBody())
                    .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)
                    .execute();
        } catch (IOException e) {
            if (e instanceof HttpStatusException && ((HttpStatusException)e).getStatusCode() == 400) {
                throw new InvalidCredentialsException("Two factor authentication failed");
            }
            throw new RuntimeException("Problem with Finalize authorization request: " + e);
        }
    }

    private static String getFinalizeAuthorizationBody() {
        return """
                {
                  "scaAuthorizationId": ""
                }
                """;
    }
}
