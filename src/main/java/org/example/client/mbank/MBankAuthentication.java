package org.example.client.mbank;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.client.exceptions.InvalidCredentials;
import org.example.client.Retrieval;
import org.example.client.Authentication;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.example.client.mbank.MBankUtils.*;

public class MBankAuthentication implements Authentication {
    private final HttpRequest.Builder request = HttpRequest.newBuilder()
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Accept-Encoding", "gzip")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
            .header("Content-Type", "application/json");
    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public MBankAuthentication() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        this.client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();
    }

    @Override
    public Retrieval login(UserInput userInput) {
        userInput.login();
        loginToMBank(userInput.getLogin(), userInput.getPassword());
        initializeTwoFactorAuthentication();
        finalizeAuthorization(userInput);
        return new MBankRetrieval(request, client);
    }

    private void loginToMBank(String login, String password) {
        HttpRequest loginRequest = request.copy()
                .uri(buildUri("https://online.mbank.pl/pl/LoginMain/Account/JsonLogin"))
                .POST(HttpRequest.BodyPublishers.ofString(getLoginRequestBody(login, password)))
                .header("Referer", "https://online.mbank.pl/pl/Login")
                .build();
        HttpResponse<String> loginResponse = executeRequest(loginRequest, client);
        LoginResponse response = run(() -> mapper.readValue(loginResponse.body(), LoginResponse.class));
        if (loginResponse.statusCode() == 200 && isIncorrectCredentials(response))
            throw new InvalidCredentials("Incorrect login credentials");
        handleIncorrectResponse(loginResponse.statusCode());
        request.header("X-Tab-Id", getXTabIdCookie(loginResponse.headers()));
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

    private boolean isIncorrectCredentials(LoginResponse response) {
        return response.errorMessageTitle() != null &&
                (response.errorMessageTitle().equals("Nieprawidłowy identyfikator lub hasło.")
                        || response.errorMessageTitle().equals("Wpisujesz błędny identyfikator lub hasło"));
    }

    private String getXTabIdCookie(HttpHeaders headers) {
        Optional<String> xTabIdCookie = headers.allValues("set-cookie").stream()
                .filter(cookie -> cookie.contains("mBank_tabId"))
                .findFirst();
        if (xTabIdCookie.isPresent()) {
            return xTabIdCookie.get().substring("mBank_tabId=".length(), xTabIdCookie.get().indexOf(";"));
        }
        throw new RuntimeException("Problem with parsing cookies");
    }

    private void initializeTwoFactorAuthentication() {
        HttpRequest initializeTwoFactorAuthentication = request.copy()
                .uri(buildUri("https://online.mbank.pl/api/authorization/initialize"))
                .POST(HttpRequest.BodyPublishers.ofString(getAuthorizationBody()))
                .build();
        HttpResponse<String> response = executeRequest(initializeTwoFactorAuthentication, client);
        handleIncorrectResponse(response.statusCode());
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

    private void finalizeAuthorization(UserInput userInput) {
        userInput.waitForInput();
        HttpRequest finalizeTwoFactorAuthentication = request
                .uri(buildUri("https://online.mbank.pl/pl/Sca/FinalizeAuthorization"))
                .POST(HttpRequest.BodyPublishers.ofString(getFinalizeAuthorizationBody()))
                .build();
        HttpResponse<String> response = executeRequest(finalizeTwoFactorAuthentication, client);
        if (response.statusCode() == 400) {
            throw new InvalidCredentials("Two factor authentication failed");
        }
        handleIncorrectResponse(response.statusCode());
    }

    private static String getFinalizeAuthorizationBody() {
        return """
                {
                  "scaAuthorizationId": ""
                }
                """;
    }
}

record LoginResponse(String errorMessageTitle) {
}
