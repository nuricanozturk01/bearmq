package com.bearmq.client.dto;

import org.springframework.util.Assert;

public record Message(byte[] body) {
  public Message {
    Assert.notNull(body, "body must not be null");
  }
}
