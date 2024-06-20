package com.kontomatik.mbank;

import com.kontomatik.exceptions.InvalidCredentials;
import com.kontomatik.ImportAccounts;
import com.kontomatik.Authentication;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.kontomatik.mbank.MBankHttpClient.*;

public class MBankAuthentication implements Authentication {

  private final SignInInput signInInput;

  public MBankAuthentication(SignInInput signInInput) {
    this.signInInput = signInInput;
  }

  @Override
  public ImportAccounts signIn() {
    LoginAndPassword loginAndPassword = signInInput.provideLoginAndPassword();
    MBankHttpClient httpClient = login(loginAndPassword);
    initializeTwoFactorAuthentication(httpClient);
    finalizeAuthentication(httpClient, signInInput);
    return new MBankImportAccounts(httpClient);
  }

  private static MBankHttpClient login(LoginAndPassword loginAndPassword) {
    String body = createLoginRequestBody(loginAndPassword);
    return MBankHttpClient.initialize(body);
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

  private static void initializeTwoFactorAuthentication(MBankHttpClient httpClient) {
    HttpRequest initializeTwoFactorAuthentication = buildInitializeTwoFactorAuthenticationRequest(httpClient);
    httpClient.fetch(initializeTwoFactorAuthentication);
  }

  private static HttpRequest buildInitializeTwoFactorAuthenticationRequest(MBankHttpClient httpClient) {
    return httpClient.prepareRequest("/api/authorization/initialize")
      .POST(HttpRequest.BodyPublishers.ofString(createAuthorizationBody()))
      .build();
  }

  private static String createAuthorizationBody() {
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

  private static void finalizeAuthentication(MBankHttpClient httpClient, SignInInput signInInput) {
    signInInput.confirmTwoFactorAuthentication();
    HttpRequest finalizeTwoFactorAuthentication = buildFinalizeTwoFactorAuthenticationRequest(httpClient);
    HttpResponse<String> response = httpClient.fetchWithoutCorrectResponseAssertion(finalizeTwoFactorAuthentication);
    if (response.statusCode() == 400) throw new InvalidCredentials("Two factor authentication failed");
    assertCorrectResponse(response.statusCode());
  }

  private static HttpRequest buildFinalizeTwoFactorAuthenticationRequest(MBankHttpClient httpClient) {
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

}
