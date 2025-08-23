package com.bearmq.client;

public class RetryableException extends RuntimeException {
  public RetryableException(String message) {
    super(message);
  }
}
