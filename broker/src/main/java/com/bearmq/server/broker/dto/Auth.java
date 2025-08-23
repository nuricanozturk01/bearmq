package com.bearmq.server.broker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Auth {
  private String vhost;
  private String username;
  private String password;
  private String apiKey;
}
