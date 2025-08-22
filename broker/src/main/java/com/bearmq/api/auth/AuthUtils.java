package com.bearmq.api.auth;

import org.springframework.data.util.Pair;

import java.util.Base64;

public final class AuthUtils {
  public static Pair<String, String> extractBasicCredentials(final String basicAuth) {
    final String basic = basicAuth.substring("Basic ".length());
    final var credentials = new String(Base64.getDecoder().decode(basic));
    final var values = credentials.split(":");
    final var username = values[0];
    final var password = values[1];

    return Pair.of(username, password);
  }

  private AuthUtils() {
  }

  public static String checkBearerTokenValidityAndGet(String bearerToken) {
    if (bearerToken == null || bearerToken.isEmpty()) {
      throw new NullPointerException("bearerToken is null");
    }

    if (!bearerToken.startsWith("Bearer ") || bearerToken.length() == "Bearer ".length()) {
      throw new IllegalArgumentException("Bearer token is invalid");
    }

    return bearerToken.substring("Bearer ".length());
  }
}
