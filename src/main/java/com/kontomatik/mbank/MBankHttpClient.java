package com.kontomatik.mbank;

import com.kontomatik.exceptions.ExceptionUtils;
import com.kontomatik.exceptions.InvalidCredentials;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import static com.kontomatik.mbank.HttpAgent.assertSuccessfulResponse;

class MBankHttpClient {

  private static final String HOST = "https://online.mbank.pl";

  private final HttpAgent httpAgent;

  private final String xTabId;

  static MBankHttpClient initialize(String body) {
    HttpAgent agent = new HttpAgent();
    HttpRequest request = buildLoginRequest(body);
    HttpResponse<LoginResponse> response = agent.fetchParsedBody(request, LoginResponse.class);
    assertSuccessfulLogin(response.body());
    String xTabId = extractXTabIdCookie(response.headers());
    return new MBankHttpClient(agent, xTabId);
  }

  private static HttpRequest buildLoginRequest(String body) {
    return baseRequest("/pl/LoginMain/Account/JsonLogin")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .header("Referer", HOST + "/pl/Login")
      .build();
  }

  private static HttpRequest.Builder baseRequest(String path) {
    return HttpRequest.newBuilder()
      .uri(buildUri(HOST + path))
      .header("X-Requested-With", "XMLHttpRequest")
      .header("Accept-Encoding", "gzip")
      .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
      .header("Content-Type", "application/json");
  }

  private static URI buildUri(String uri) {
    return ExceptionUtils.uncheck(() -> new URI(uri));
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
      .orElseThrow(RuntimeException::new);
  }

  private MBankHttpClient(HttpAgent httpAgent, String xTabId) {
    this.httpAgent = httpAgent;
    this.xTabId = xTabId;
  }

  HttpRequest.Builder prepareRequest(String path) {
    return baseRequest(path).header("X-Tab-Id", Objects.requireNonNull(xTabId));
  }

  void fetch(HttpRequest request) {
    httpAgent.fetch(request);
  }

  void fetchFinalizeTwoFactor(HttpRequest request) {
    HttpResponse<String> response = httpAgent.fetchWithoutCorrectResponseAssertion(request);
    if (response.statusCode() == 400) throw new InvalidCredentials("Two factor authentication failed");
    assertSuccessfulResponse(response.statusCode());
  }

  <T> HttpResponse<T> fetchParsedBody(HttpRequest request, Class<T> outputClass) {
    return httpAgent.fetchParsedBody(request, outputClass);
  }

  void fetchWithoutCorrectResponseAssertion(HttpRequest request) {
    httpAgent.fetchWithoutCorrectResponseAssertion(request);
  }

  private record LoginResponse(String errorMessageTitle) {
  }
  
}