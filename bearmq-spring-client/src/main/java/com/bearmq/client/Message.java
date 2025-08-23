package com.bearmq.client;

import org.springframework.util.Assert;

public record Message(byte[] body) {
  public Message {
    Assert.notNull(body, "body must not be null");
  }
}
