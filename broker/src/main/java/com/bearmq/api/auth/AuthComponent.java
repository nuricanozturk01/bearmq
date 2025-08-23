package com.bearmq.api.auth;

import com.bearmq.api.auth.dto.AuthRequest;
import com.bearmq.api.auth.dto.AuthResponse;
import com.bearmq.api.tenant.TenantService;
import com.bearmq.api.tenant.TenantStatus;
import com.bearmq.api.tenant.dto.TenantInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AuthComponent {
  private final TenantService tenantService;

  public AuthResponse authenticate(final AuthRequest authRequest) {
    final var user = tenantService.findByUsername(authRequest.username());

    if (user.status() == TenantStatus.DELETED || user.status() == TenantStatus.SUSPENDED) {
      throw new RuntimeException("User is deleted or suspended");
    }

    final var hashedPassword = DigestUtils.sha256Hex(user.salt() + authRequest.password());
    final var userPasswordHash = user.password();

    if (!userPasswordHash.equals(hashedPassword)) {
      throw new RuntimeException("invalid username or password");
    }

    return AuthResponse.builder()
            .token("token123")
            .refreshToken("refreshToken123")
            .build();
  }

  // Sample code. We need skip auth parts for accelerate development
  public TenantInfo authorize(final String apiKey, String token) {
    // extract username in token.
    return tenantService.findByApiKey(apiKey);
  }

  public TenantInfo authorize(final String apiKey) {
    return tenantService.findByApiKey(apiKey);
  }
}
