package com.kontomatik.mbank;

import com.kontomatik.exceptions.ExceptionUtils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

class MBankHttpClient {

  private static final String HOST = "https://online.mbank.pl";

  private final HttpAgent httpAgent;

  private final String xTabId;

  MBankHttpClient(HttpAgent httpAgent, String xTabId) {
    this.httpAgent = httpAgent;
    this.xTabId = xTabId;
  }

  HttpRequest.Builder prepareRequest(String path) {
    return baseRequest(path).header("X-Tab-Id", Objects.requireNonNull(xTabId));
  }

  static HttpRequest.Builder baseRequest(String path) {
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

  void fetch(HttpRequest request) {
    httpAgent.fetch(request);
  }

  <T> HttpResponse<T> fetchParsedBody(HttpRequest request, Class<T> outputClass) {
    return httpAgent.fetchParsedBody(request, outputClass);
  }

  HttpResponse<String> fetchWithoutCorrectResponseAssertion(HttpRequest request) {
    return httpAgent.fetchWithoutCorrectResponseAssertion(request);
  }
  
}