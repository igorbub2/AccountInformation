package com.kontomatik.mbank;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kontomatik.exceptions.ExceptionUtils;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.charset.Charset;

class HttpAgent {

  private final HttpClient httpClient;

  private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public HttpAgent() {
    var cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    httpClient = HttpClient.newBuilder().cookieHandler(cookieManager).build();
  }

  <T> HttpResponse<T> fetchParsedBody(HttpRequest request, Class<T> outputClass) {
    return ExceptionUtils.uncheck(() -> httpClient.send(
      request,
      responseInfo -> BodySubscribers.mapping(BodySubscribers.ofString(Charset.defaultCharset()), body -> parse(body, outputClass))
    ));
  }

  private static <T> T parse(String body, Class<T> outputClass) {
    return ExceptionUtils.uncheck(() -> MAPPER.readValue(body, outputClass));
  }

  void fetch(HttpRequest request) {
    HttpResponse<String> response = fetchWithoutCorrectResponseAssertion(request);
    assertSuccessfulResponse(response.statusCode());
  }

  HttpResponse<String> fetchWithoutCorrectResponseAssertion(HttpRequest request) {
    return ExceptionUtils.uncheck(() -> httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
  }

  static void assertSuccessfulResponse(int statusCode) {
    if (statusCode < 200 || statusCode >= 300) throw new RuntimeException();
  }

}
