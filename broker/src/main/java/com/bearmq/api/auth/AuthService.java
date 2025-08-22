package com.bearmq.api.auth;

import com.bearmq.api.auth.dto.AuthRequest;
import com.bearmq.api.auth.dto.AuthResponse;
import com.bearmq.api.tenant.TenantRepository;
import com.bearmq.api.tenant.converter.TenantConverter;
import com.bearmq.api.tenant.dto.TenantInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {
  public AuthResponse authenticate(final TenantInfo tenantInfo) {
    return AuthResponse.builder()
            .token("token123")
            .refreshToken("refreshToken123")
            .build();
  }

  public AuthResponse authenticate(final AuthRequest authRequest) {
    // I Skip validation for now

    return AuthResponse.builder()
            .token("token123")
            .refreshToken("refreshToken123")
            .build();
  }

  // Sample code. We need skip auth parts for accelerate development
  public Pair<String, String> authorize(final String basicAuth) {
    final String basic = basicAuth.substring("Basic ".length());
    final var credentials = new String(Base64.getDecoder().decode(basic));
    final var values = credentials.split(":");
    final var username = values[0];
    final var password = values[1];

    return Pair.of(username, password);
  }
}
