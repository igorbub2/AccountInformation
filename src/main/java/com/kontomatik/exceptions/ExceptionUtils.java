package com.kontomatik.exceptions;

import java.util.concurrent.Callable;

public class ExceptionUtils {

  public static <T> T uncheck(Callable<T> callable) {
    try {
      return callable.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
