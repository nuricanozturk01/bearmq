package com.bearmq.client;

public class BearMQException extends RuntimeException {
  public BearMQException(final String message) {
    super(message);
  }

  public BearMQException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
