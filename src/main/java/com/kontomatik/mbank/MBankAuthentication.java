package com.kontomatik.mbank;

import com.kontomatik.ImportAccounts;
import com.kontomatik.Authentication;
import com.kontomatik.exceptions.InvalidCredentials;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.kontomatik.mbank.HttpAgent.assertSuccessfulResponse;

public class MBankAuthentication implements Authentication {

  private final SignInInput signInInput;

  public MBankAuthentication(SignInInput signInInput) {
    this.signInInput = signInInput;
  }

  @Override
  public ImportAccounts signIn() {
    MBankHttpClient httpClient = login();
    initializeTwoFactorAuthentication(httpClient);
    finalizeAuthentication(httpClient);
    return new MBankImportAccounts(httpClient);
  }

  private MBankHttpClient login() {
    LoginAndPassword loginAndPassword = signInInput.provideLoginAndPassword();
    var agent = new HttpAgent();
    HttpRequest request = buildLoginRequest(loginAndPassword);
    HttpResponse<LoginResponse> response = agent.fetchParsedBody(request, LoginResponse.class);
    assertSuccessfulLogin(response.body());
    String xTabId = extractXTabIdCookie(response.headers());
    return new MBankHttpClient(agent, xTabId);
  }

  private static HttpRequest buildLoginRequest(LoginAndPassword loginAndPassword) {
    return MBankHttpClient.baseRequest("/pl/LoginMain/Account/JsonLogin")
      .POST(HttpRequest.BodyPublishers.ofString(createLoginRequestBody(loginAndPassword)))
      .header("Referer", "https://online.mbank.pl/pl/Login")
      .build();
  }

  private static String createLoginRequestBody(LoginAndPassword loginAndPassword) {
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
    return String.format(requestBody, loginAndPassword.login(), loginAndPassword.password());
  }

  private static void assertSuccessfulLogin(LoginResponse response) {
    if (response.errorMessageTitle() != null &&
      (response.errorMessageTitle().equals("Nieprawidłowy identyfikator lub hasło.")
        || response.errorMessageTitle().equals("Wpisujesz błędny identyfikator lub hasło")))
      throw new InvalidCredentials("Incorrect login credentials");
  }

  private static String extractXTabIdCookie(HttpHeaders headers) {
    return headers.allValues("set-cookie").stream()
      .filter(cookie -> cookie.contains("mBank_tabId"))
      .findFirst()
      .map(cookie -> cookie.substring("mBank_tabId=".length(), cookie.indexOf(";")))
      .orElseThrow();
  }

  private static void initializeTwoFactorAuthentication(MBankHttpClient httpClient) {
    HttpRequest initTwoFactorRequest = buildInitTwoFactorRequest(httpClient);
    httpClient.fetch(initTwoFactorRequest);
  }

  private static HttpRequest buildInitTwoFactorRequest(MBankHttpClient httpClient) {
    return httpClient.prepareRequest("/api/authorization/initialize")
      .POST(HttpRequest.BodyPublishers.ofString(createInitTwoFactorBody()))
      .build();
  }

  private static String createInitTwoFactorBody() {
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

  private void finalizeAuthentication(MBankHttpClient httpClient) {
    signInInput.confirmTwoFactorAuthentication();
    HttpRequest finalizeTwoFactorRequest = buildFinalizeTwoFactorRequest(httpClient);
    HttpResponse<String> response = httpClient.fetchWithoutCorrectResponseAssertion(finalizeTwoFactorRequest);
    if (response.statusCode() == 400) throw new InvalidCredentials("Two factor authentication failed");
    assertSuccessfulResponse(response.statusCode());
  }

  private static HttpRequest buildFinalizeTwoFactorRequest(MBankHttpClient httpClient) {
    return httpClient.prepareRequest("/pl/Sca/FinalizeAuthorization")
      .POST(HttpRequest.BodyPublishers.ofString(createFinalizeAuthorizationBody()))
      .build();
  }

  private static String createFinalizeAuthorizationBody() {
    return """
      {
        "scaAuthorizationId": ""
      }
      """;
  }

  private record LoginResponse(String errorMessageTitle) {
  }

}
