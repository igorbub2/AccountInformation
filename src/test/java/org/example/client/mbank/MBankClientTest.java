package org.example.client.mbank;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.example.client.BankClient.Status;
import org.example.model.AccountInformation;
import org.jsoup.HttpStatusException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class MBankClientTest {
    private final String HOST = "localhost";
    private final int PORT = 8080;
    private final String HOST_WITH_PORT = "http://" + HOST + ":" + PORT;

    private static final MBankClient client = new MBankClient();

    private final WireMockServer server = new WireMockServer();

    @BeforeEach
    void setUp() {
        client.HOST = HOST_WITH_PORT;
        server.start();
        configureFor(HOST, PORT);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    public void shouldSuccessfullyLogin() throws IOException {
        client.userInput = () -> {
        };
        Status preLoginStatus = client.getStatus();
        server.stubFor(post("/pl/LoginMain/Account/JsonLogin").willReturn(aResponse().withStatus(200)));
        server.stubFor(post("/api/authorization/initialize").willReturn(aResponse().withStatus(200)));
        server.stubFor(post("/pl/Sca/FinalizeAuthorization").willReturn(aResponse().withStatus(200)));

        client.login("testLogin", "testPassword");

        Status postLoginStatus = client.getStatus();

        verify(exactly(1), postRequestedFor(urlEqualTo("/pl/LoginMain/Account/JsonLogin")));
        verify(exactly(1), postRequestedFor(urlEqualTo("/api/authorization/initialize")));
        verify(exactly(1), postRequestedFor(urlEqualTo("/pl/Sca/FinalizeAuthorization")));

        assertEquals(preLoginStatus, Status.LOGGED_OUT);
        assertEquals(postLoginStatus, Status.LOGGED_IN);
    }

    @Test
    public void shouldFailLogin() {
        server.stubFor(post("/pl/LoginMain/Account/JsonLogin").willReturn(aResponse().withStatus(200)));
        server.stubFor(post("/api/authorization/initialize").willReturn(unauthorized()));
        server.stubFor(post("/pl/Sca/FinalizeAuthorization").willReturn(aResponse().withStatus(200)));

        HttpStatusException exception =
                assertThrows(HttpStatusException.class, () -> client.login("testLogin", "testPassword"));

        verify(exactly(1), postRequestedFor(urlEqualTo("/pl/LoginMain/Account/JsonLogin")));
        verify(exactly(1), postRequestedFor(urlEqualTo("/api/authorization/initialize")));
        verify(exactly(0), postRequestedFor(urlEqualTo("/pl/Sca/FinalizeAuthorization")));

        assertEquals(exception.getStatusCode(), 401);
        assertEquals(client.getStatus(), Status.LOGGED_OUT);
    }

    @Test
    public void shouldReturnAccountInformation() throws IOException {
        server.stubFor(get("/pl/Accounts/Accounts/AccountsGroups").willReturn(aResponse().withBody(getAccountsGroupsBody()).withStatus(200)));

        List<AccountInformation> accountInformation = client.retrieveAccountsBalance();

        verify(exactly(1), getRequestedFor(urlEqualTo("/pl/Accounts/Accounts/AccountsGroups")));

        assertEquals(accountInformation.size(), 3);
    }

    @Test
    public void shouldThrowParsingException() {
        server.stubFor(get("/pl/Accounts/Accounts/AccountsGroups").willReturn(aResponse().withBody("").withStatus(200)));

        RuntimeException exception = assertThrows(RuntimeException.class, client::retrieveAccountsBalance);

        verify(exactly(1), getRequestedFor(urlEqualTo("/pl/Accounts/Accounts/AccountsGroups")));

        assertTrue(exception.getMessage().startsWith("Cannot retrieve accounts groups"));

    }

    @Test
    public void shouldLogOut() throws IOException {
        client.status = Status.LOGGED_IN;
        server.stubFor(get("/LoginMain/Account/Logout").willReturn(aResponse().withStatus(200)));

        client.logout();

        verify(exactly(1), getRequestedFor(urlEqualTo("/LoginMain/Account/Logout")));
        assertEquals(client.getStatus(), Status.LOGGED_OUT);
    }

    private String getAccountsGroupsBody() {
        String requestBody;
        try {
            requestBody = Files.readString(Paths.get("src/test/resources/TestAccountsGroupsBody.json"));
        } catch (IOException e) {
            throw new RuntimeException("Cannot retrieve file for Accounts Groups Body");
        }
        return requestBody;
    }
}