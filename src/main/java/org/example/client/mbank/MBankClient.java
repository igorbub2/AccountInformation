package org.example.client.mbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.client.BankClient;
import org.example.model.AccountInformation;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MBankClient implements BankClient {
    protected UserInput userInput = new KeyboardUserInput();
    protected String HOST = "https://online.mbank.pl";
    Status status = Status.LOGGED_OUT;
    private final Connection connection = Jsoup.newSession();
    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final String LOGIN_POST_REQUEST_URL = "/pl/LoginMain/Account/JsonLogin";
    private static final String INITIALIZE_2FA_URL = "/api/authorization/initialize";
    private static final String FINALIZE_AUTHORIZATION_URL = "/pl/Sca/FinalizeAuthorization";
    private static final String ACCOUNTS_GROUP_URL = "/pl/Accounts/Accounts/AccountsGroups";
    private static final String LOGOUT_URL = "/LoginMain/Account/Logout";
    private static final String HEADER_X_REQUESTED_WITH = "X-Requested-With";
    private static final String HEADER_X_REQUESTED_WITH_VALUE = "XMLHttpRequest";
    private static final String HEADER_X_TAB_ID = "X-Tab-Id";
    private static final String COOKIE_MBANK_TAB_ID = "mBank_tabId";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_TYPE_VALUE = "application/json";

    @Override
    public void login(String login, String password) throws IOException {
        connection.header(HEADER_X_REQUESTED_WITH, HEADER_X_REQUESTED_WITH_VALUE);
        attemptLogin(connection, login, password);
        initializeTwoFactorAuthentication(connection);
        finalizeAuthorization(connection);
    }

    @Override
    public List<AccountInformation> retrieveAccountsBalance() throws IOException {
        Connection.Response accountsGroup = connection.newRequest(HOST + ACCOUNTS_GROUP_URL)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute();

        MBankAccountsGroups accountsGroups;
        try {
            accountsGroups = mapper.readValue(accountsGroup.body(), MBankAccountsGroups.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot retrieve accounts groups " + e);
        }

        return accountsGroups.accountsGroups().stream()
                .flatMap(accountGroup -> accountGroup.accounts().stream()
                        .map(account -> new AccountInformation(account.accountNumber(), account.balance(), account.currency())))
                .collect(Collectors.toList());
    }

    @Override
    public void logout() throws IOException {
        try {
            connection.newRequest(HOST + LOGOUT_URL)
                    .method(Connection.Method.GET)
                    .execute();
        } finally {
            status = Status.LOGGED_OUT;
        }
    }

    @Override
    public Status getStatus() {
        return status;
    }

    private void attemptLogin(Connection connection, String login, String password) throws IOException {
        Connection.Response loginResponse = connection.newRequest(HOST + LOGIN_POST_REQUEST_URL)
                .requestBody(getLoginRequestBody(login, password))
                .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();

        connection.header(HEADER_X_TAB_ID, loginResponse.cookies().get(COOKIE_MBANK_TAB_ID));
    }

    private void initializeTwoFactorAuthentication(Connection connection) throws IOException {
        connection.newRequest(HOST + INITIALIZE_2FA_URL)
                .method(Connection.Method.POST)
                .requestBody(getAuthorizationBody())
                .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)
                .execute();
    }

    private void finalizeAuthorization(Connection connection) throws IOException {
        userInput.waitForInput();

        connection.newRequest(HOST + FINALIZE_AUTHORIZATION_URL)
                .method(Connection.Method.POST)
                .requestBody(getFinalizeAuthorizationBody())
                .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)
                .execute();

        status = Status.LOGGED_IN;
    }

    private static String getLoginRequestBody(String login, String password) {
        String requestBody;
        try {
            requestBody = Files.readString(Paths.get("src/main/resources/LoginRequestBody.json"));
        } catch (IOException e) {
            throw new RuntimeException("Cannot retrieve file for Login request body");
        }
        return String.format(requestBody, login, password);
    }

    private static String getAuthorizationBody() {
        String requestBody;
        try {
            requestBody = Files.readString(Paths.get("src/main/resources/Initialize2FaRequestBody.json"));
        } catch (IOException e) {
            throw new RuntimeException("Cannot retrieve file for Initialize 2FA request body");
        }
        return requestBody;
    }

    private static String getFinalizeAuthorizationBody() {
        String requestBody;
        try {
            requestBody = Files.readString(Paths.get("src/main/resources/FinalizeAuthorizationRequestBody.json"));
        } catch (IOException e) {
            throw new RuntimeException("Cannot retrieve file for Finalize Authorization request body");
        }
        return requestBody;
    }
}
