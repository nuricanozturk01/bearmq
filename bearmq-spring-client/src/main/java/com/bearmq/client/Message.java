package com.bearmq.client;

import org.springframework.util.Assert;

public class Message {
  private final byte[] body;

  public Message(byte[] body) {
    Assert.notNull(body, "body must not be null");
    this.body = body;
  }

  public byte[] getBody() {
    return body;
  }
}
