package com.kontomatik.mbank;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kontomatik.exceptions.ExceptionUtils;
import com.kontomatik.exceptions.InvalidCredentials;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class MBankHttpClient {

  final static String HOST = "https://online.mbank.pl";

  private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private final HttpClient client;

  private String xTabId;

  private MBankHttpClient() {
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    client = HttpClient.newBuilder()
      .cookieHandler(cookieManager)
      .build();
  }

  static MBankHttpClient initialize(String body) {
    MBankHttpClient httpClient = new MBankHttpClient();
    HttpRequest request = buildLoginRequest(body);
    HttpResponse<String> loginResponse = httpClient.fetchRequest(request);
    LoginResponse response = httpClient.parse(loginResponse.body(), LoginResponse.class);
    if (httpClient.isIncorrectCredentials(response)) throw new InvalidCredentials("Incorrect login credentials");
    httpClient.xTabId = httpClient.extractXTabIdCookie(loginResponse.headers());
    return httpClient;
  }

  private static HttpRequest buildLoginRequest(String body) {
    return baseRequest()
      .uri(buildUri(HOST + "/pl/LoginMain/Account/JsonLogin"))
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .header("Referer", "https://online.mbank.pl/pl/Login")
      .build();
  }

  private static HttpRequest.Builder baseRequest() {
    return HttpRequest.newBuilder()
      .header("X-Requested-With", "XMLHttpRequest")
      .header("Accept-Encoding", "gzip")
      .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
      .header("Content-Type", "application/json");
  }

  private boolean isIncorrectCredentials(LoginResponse response) {
    return response.errorMessageTitle() != null &&
      (response.errorMessageTitle().equals("Nieprawidłowy identyfikator lub hasło.")
        || response.errorMessageTitle().equals("Wpisujesz błędny identyfikator lub hasło"));
  }


  private String extractXTabIdCookie(HttpHeaders headers) {
    return headers.allValues("set-cookie").stream()
      .filter(cookie -> cookie.contains("mBank_tabId"))
      .findFirst()
      .map(cookie -> cookie.substring("mBank_tabId=".length(), cookie.indexOf(";")))
      .orElseThrow(() -> new RuntimeException("Problem with parsing cookies"));
  }

  HttpRequest.Builder getRequest() {
    return baseRequest().header("X-Tab-Id", Objects.requireNonNull(xTabId));
  }

  static URI buildUri(String uri) {
    return ExceptionUtils.uncheck(() -> new URI(uri));
  }

  HttpResponse<String> fetchRequest(HttpRequest request) {
    HttpResponse<String> response = fetchRequestWithoutIncorrectResponseHandling(request);
    handleIncorrectResponse(response.statusCode());
    return response;
  }

  HttpResponse<String> fetchRequestWithoutIncorrectResponseHandling(HttpRequest request) {
    return ExceptionUtils.uncheck(() -> client.send(request, HttpResponse.BodyHandlers.ofString()));
  }

  static void handleIncorrectResponse(int statusCode) {
    if (statusCode < 200 || statusCode >= 300) throw new RuntimeException();
  }

  <T> T parse(String body, Class<T> outputClass) {
    return ExceptionUtils.uncheck(() -> mapper.readValue(body, outputClass));
  }

  private record LoginResponse(String errorMessageTitle) {
  }
}