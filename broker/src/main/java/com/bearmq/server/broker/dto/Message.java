package com.bearmq.server.broker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Message {
  private Auth auth;
  private String operation;
  private String queue;
  private String exchange;
  private String body;
}
