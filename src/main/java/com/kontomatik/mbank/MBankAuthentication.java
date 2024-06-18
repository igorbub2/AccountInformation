package com.kontomatik.mbank;

import com.kontomatik.exceptions.InvalidCredentials;
import com.kontomatik.Retrieval;
import com.kontomatik.Authentication;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.kontomatik.mbank.MBankHttpClient.*;

public class MBankAuthentication implements Authentication {

  private final SignInInput signInInput;
  private MBankHttpClient httpClient;

  public MBankAuthentication(SignInInput signInInput) {
    this.signInInput = signInInput;
  }

  @Override
  public Retrieval signIn() {
    LoginCredentials loginCredentials = signInInput.provideCredentials();
    login(loginCredentials);
    initializeTwoFactorAuthentication();
    finalizeAuthentication(signInInput);
    return new MBankRetrieval(httpClient);
  }

  private void login(LoginCredentials loginCredentials) {
    URI uri = buildUri(HOST + "/pl/LoginMain/Account/JsonLogin");
    String body = getLoginRequestBody(loginCredentials);
    httpClient = MBankHttpClient.initialize(uri, body);
  }

  private static String getLoginRequestBody(LoginCredentials loginCredentials) {
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
    return String.format(requestBody, loginCredentials.login(), loginCredentials.password());
  }

  private void initializeTwoFactorAuthentication() {
    HttpRequest initializeTwoFactorAuthentication = httpClient.getRequest()
      .uri(buildUri(HOST + "/api/authorization/initialize"))
      .POST(HttpRequest.BodyPublishers.ofString(getAuthorizationBody()))
      .build();
    httpClient.fetchRequest(initializeTwoFactorAuthentication);
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

  private void finalizeAuthentication(SignInInput signInInput) {
    signInInput.confirmTwoFactorAuthentication();
    HttpRequest finalizeTwoFactorAuthentication = buildFinalizeTwoFactorAuthenticationRequest();
    HttpResponse<String> response = httpClient.fetchRequestWithoutResponseHandling(finalizeTwoFactorAuthentication);
    if (response.statusCode() == 400) throw new InvalidCredentials("Two factor authentication failed");
    handleIncorrectResponse(response.statusCode());
  }

  private HttpRequest buildFinalizeTwoFactorAuthenticationRequest() {
    return httpClient.getRequest()
      .uri(buildUri(HOST + "/pl/Sca/FinalizeAuthorization"))
      .POST(HttpRequest.BodyPublishers.ofString(getFinalizeAuthorizationBody()))
      .build();
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
