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

class MBankHttpClient {

  private static final String HOST = "https://online.mbank.pl";

  private static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private final HttpClient client;

  private String xTabId;

  static MBankHttpClient initialize(String body) {
    MBankHttpClient httpClient = new MBankHttpClient();
    HttpRequest request = buildLoginRequest(body);
    HttpResponse<String> loginResponse = httpClient.fetch(request);
    LoginResponse response = parse(loginResponse.body(), LoginResponse.class);
    if (isIncorrectCredentials(response)) throw new InvalidCredentials("Incorrect login credentials");
    httpClient.xTabId = extractXTabIdCookie(loginResponse.headers());
    return httpClient;
  }

  private MBankHttpClient() {
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    client = HttpClient.newBuilder()
      .cookieHandler(cookieManager)
      .build();
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

  HttpResponse<String> fetch(HttpRequest request) {
    HttpResponse<String> response = fetchWithoutCorrectResponseAssertion(request);
    assertCorrectResponse(response.statusCode());
    return response;
  }

  HttpResponse<String> fetchWithoutCorrectResponseAssertion(HttpRequest request) {
    return ExceptionUtils.uncheck(() -> client.send(request, HttpResponse.BodyHandlers.ofString()));
  }

  static void assertCorrectResponse(int statusCode) {
    if (statusCode < 200 || statusCode >= 300) throw new RuntimeException();
  }

  private static <T> T parse(String body, Class<T> outputClass) {
    return ExceptionUtils.uncheck(() -> mapper.readValue(body, outputClass));
  }

  private static boolean isIncorrectCredentials(LoginResponse response) {
    return response.errorMessageTitle() != null &&
      (response.errorMessageTitle().equals("Nieprawidłowy identyfikator lub hasło.")
        || response.errorMessageTitle().equals("Wpisujesz błędny identyfikator lub hasło"));
  }

  private static String extractXTabIdCookie(HttpHeaders headers) {
    return headers.allValues("set-cookie").stream()
      .filter(cookie -> cookie.contains("mBank_tabId"))
      .findFirst()
      .map(cookie -> cookie.substring("mBank_tabId=".length(), cookie.indexOf(";")))
      .orElseThrow(RuntimeException::new);
  }

  HttpRequest.Builder prepareRequest(String path) {
    return baseRequest(path).header("X-Tab-Id", Objects.requireNonNull(xTabId));
  }

  <T> T fetchParsedBody(HttpRequest request, Class<T> outputClass) {
    String body = fetch(request).body();
    return parse(body, outputClass);
  }

  private record LoginResponse(String errorMessageTitle) {
  }
  
}