package org.example.client.mbank;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.example.model.Account;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class LoggedInMBankClientTest {
    private final String HOST = "localhost";
    private final int PORT = 8080;
    private final String HOST_WITH_PORT = "http://" + HOST + ":" + PORT;
    private final LoggedInMBankClient client = new LoggedInMBankClient(Jsoup.newSession(), HOST_WITH_PORT);
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
    public void shouldReturnAccountInformation() {
        server.stubFor(get("/pl/Accounts/Accounts/AccountsGroups").willReturn(aResponse().withBody(getAccountsGroupsBody()).withStatus(200)));
        List<Account> account = client.retrieveAccountsBalance();
        verify(exactly(1), getRequestedFor(urlEqualTo("/pl/Accounts/Accounts/AccountsGroups")));
        assertEquals(account.size(), 3);
    }

    @Test
    public void shouldThrowParsingException() {
        server.stubFor(get("/pl/Accounts/Accounts/AccountsGroups").willReturn(aResponse().withBody("").withStatus(200)));
        RuntimeException exception = assertThrows(RuntimeException.class, client::retrieveAccountsBalance);
        verify(exactly(1), getRequestedFor(urlEqualTo("/pl/Accounts/Accounts/AccountsGroups")));
        assertTrue(exception.getMessage().startsWith("Cannot retrieve accounts groups"));

    }

    @Test
    public void shouldLogOut() {
        server.stubFor(get("/LoginMain/Account/Logout").willReturn(aResponse().withStatus(200)));
        client.logout();
        verify(exactly(1), getRequestedFor(urlEqualTo("/LoginMain/Account/Logout")));
    }

    private String getAccountsGroupsBody() {
        return """
                {
                  "accountsGroups": [
                    {
                      "accounts": [
                        {
                          "accountNumber": "12 1234 5678 1234 5678 1234 5678",
                          "balance": 500.00,
                          "currency": "PLN",
                          "name": "mKonto Intensive",
                          "customName": "",
                          "primaryAction": {
                            "url": "/pl/Accounts/Accounts/GetTransferDomestic",
                            "name": null
                          },
                          "id": "testId1"
                        }
                      ],
                      "header": "Personal",
                      "summary": {
                        "currency": "PLN",
                        "isRoundedToOneCurrency": false,
                        "balance": 500.00
                      }
                    },
                    {
                      "accounts": [
                        {
                          "accountNumber": "56 1234 5678 1234 5678 1234 5678",
                          "balance": 1000.00,
                          "currency": "PLN",
                          "name": "mKonto Intensive",
                          "customName": "",
                          "primaryAction": {
                            "url": "/pl/Accounts/Accounts/GetTransferDomestic",
                            "name": null
                          },
                          "id": "testId2"
                        }
                      ],
                      "header": "Vat",
                      "summary": {
                        "currency": "PLN",
                        "isRoundedToOneCurrency": false,
                        "balance": 1000.00
                      }
                    },
                    {
                      "accounts": [],
                      "header": "Foreigns",
                      "summary": {
                        "currency": "PLN",
                        "isRoundedToOneCurrency": false,
                        "balance": 0.0
                      }
                    },
                    {
                      "accounts": [
                        {
                          "accountNumber": "78 1234 5678 1234 5678 1234 5678",
                          "balance": 12432.50,
                          "currency": "PLN",
                          "name": "mKonto Intensive",
                          "customName": "",
                          "primaryAction": {
                            "url": "/pl/Accounts/Accounts/GetTransferDomestic",
                            "name": null
                          },
                          "id": "testId3"
                        }
                      ],
                      "header": "Authorities",
                      "summary": {
                        "currency": "PLN",
                        "isRoundedToOneCurrency": false,
                        "balance": 12432.50
                      }
                    },
                    {
                      "accounts": [],
                      "header": "Others",
                      "summary": {
                        "currency": "PLN",
                        "isRoundedToOneCurrency": false,
                        "balance": 0.0
                      }
                    }
                  ],
                  "summary": {
                    "currency": "PLN",
                    "isRoundedToOneCurrency": false,
                    "balance": 13932.50
                  }
                }
                """;
    }
}