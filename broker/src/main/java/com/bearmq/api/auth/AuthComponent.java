package com.bearmq.api.auth;

import com.bearmq.api.auth.dto.AuthRequest;
import com.bearmq.api.auth.dto.AuthResponse;
import com.bearmq.api.tenant.TenantService;
import com.bearmq.api.tenant.dto.TenantInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AuthComponent {
  private final TenantService tenantService;

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
  public TenantInfo authorize(final String apiKey, String token) {
    return tenantService.findByApiKey(apiKey);
  }
}
