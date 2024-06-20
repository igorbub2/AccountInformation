package com.kontomatik.exceptions;

import java.util.concurrent.Callable;

public class ExceptionUtils {

  public static void uncheck(ThrowingRunnable runnable) {
    uncheck(() -> {
      runnable.run();
      return null;
    });
  }

  public static <T> T uncheck(Callable<T> callable) {
    try {
      return callable.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
