package com.kontomatik.exceptions;

@FunctionalInterface
public interface ThrowingRunnable {

  void run() throws Exception;

}
