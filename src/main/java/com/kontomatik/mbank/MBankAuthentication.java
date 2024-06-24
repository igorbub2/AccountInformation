package com.kontomatik.mbank;

import com.kontomatik.ImportAccounts;
import com.kontomatik.Authentication;

import java.net.http.HttpRequest;

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
    finalizeAuthentication(httpClient);
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
    httpClient.fetchFinalizeTwoFactor(finalizeTwoFactorRequest);
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

}
