package org.example.client.mbank;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.example.client.exceptions.InvalidCredentialsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class LoggedOutMBankClientTest {
    private final String HOST = "localhost";
    private final int PORT = 8080;
    private final String HOST_WITH_PORT = "http://" + HOST + ":" + PORT;
    private final LoggedOutMBankClient client = new LoggedOutMBankClient(() -> {}, HOST_WITH_PORT);
    private final WireMockServer server = new WireMockServer();

    @BeforeEach
    void setUp() {
        server.start();
        configureFor(HOST, PORT);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    public void shouldSuccessfullyLogin() {
        server.stubFor(post("/pl/LoginMain/Account/JsonLogin").willReturn(aResponse().withStatus(200)));
        server.stubFor(post("/api/authorization/initialize").willReturn(aResponse().withStatus(200)));
        server.stubFor(post("/pl/Sca/FinalizeAuthorization").willReturn(aResponse().withStatus(200)));
        client.login("testLogin", "testPassword");
        verify(exactly(1), postRequestedFor(urlEqualTo("/pl/LoginMain/Account/JsonLogin")));
        verify(exactly(1), postRequestedFor(urlEqualTo("/api/authorization/initialize")));
        verify(exactly(1), postRequestedFor(urlEqualTo("/pl/Sca/FinalizeAuthorization")));
    }

    @Test
    public void shouldThrowInvalidCredentialsException() {
        assertThrows(InvalidCredentialsException.class, () -> client.login("123", "123"));
    }

    @Test
    public void shouldFailLogin() {
        server.stubFor(post("/pl/LoginMain/Account/JsonLogin").willReturn(aResponse().withStatus(519)));
        server.stubFor(post("/api/authorization/initialize").willReturn(aResponse().withStatus(200)));
        server.stubFor(post("/pl/Sca/FinalizeAuthorization").willReturn(aResponse().withStatus(200)));
        Exception exception = assertThrows(InvalidCredentialsException.class, () -> client.login("testLogin", "testPassword"));
        verify(exactly(1), postRequestedFor(urlEqualTo("/pl/LoginMain/Account/JsonLogin")));
        verify(exactly(0), postRequestedFor(urlEqualTo("/api/authorization/initialize")));
        verify(exactly(0), postRequestedFor(urlEqualTo("/pl/Sca/FinalizeAuthorization")));
        assertTrue(exception.getMessage().startsWith("Incorrect login credentials"));
    }
    @Test
    public void shouldFailTwoFactorAuthentication() {
        server.stubFor(post("/pl/LoginMain/Account/JsonLogin").willReturn(aResponse().withStatus(200)));
        server.stubFor(post("/api/authorization/initialize").willReturn(aResponse().withStatus(200)));
        server.stubFor(post("/pl/Sca/FinalizeAuthorization").willReturn(aResponse().withStatus(400)));
        Exception exception = assertThrows(InvalidCredentialsException.class, () -> client.login("testLogin", "testPassword"));
        verify(exactly(1), postRequestedFor(urlEqualTo("/pl/LoginMain/Account/JsonLogin")));
        verify(exactly(1), postRequestedFor(urlEqualTo("/api/authorization/initialize")));
        verify(exactly(1), postRequestedFor(urlEqualTo("/pl/Sca/FinalizeAuthorization")));
        assertTrue(exception.getMessage().startsWith("Two factor authentication failed"));
    }

    @Test
    public void shouldThrowExceptionWhenLoggingInToMbank() {
        LoggedOutMBankClient mBankClient = new LoggedOutMBankClient(() -> {}, "https://online.mbank.pl");
        assertThrows(InvalidCredentialsException.class, () -> mBankClient.login("testLogin", "testPassword"));
    }
}