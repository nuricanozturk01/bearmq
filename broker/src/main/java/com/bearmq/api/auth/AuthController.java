package com.bearmq.api.auth;

import com.bearmq.api.auth.dto.AuthRequest;
import com.bearmq.api.auth.dto.AuthResponse;
import com.bearmq.api.auth.dto.RegisterRequest;
import com.bearmq.api.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {
  private final AuthComponent authComponent;
  private final TenantService tenantService;


  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@RequestBody final RegisterRequest registerRequest) {
    final var tenantInfo = tenantService.create(registerRequest);

    return ResponseEntity.ok(AuthResponse.builder()
            .token("token123")
            .refreshToken("refreshToken123")
            .build());
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(final @RequestBody AuthRequest authRequest) {
    return ResponseEntity.ok(authComponent.authenticate(authRequest));
  }
}
