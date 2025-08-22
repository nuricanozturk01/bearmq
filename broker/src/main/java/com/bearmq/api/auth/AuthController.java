package com.bearmq.api.auth;

import com.bearmq.api.auth.dto.AuthRequest;
import com.bearmq.api.auth.dto.AuthResponse;
import com.bearmq.api.auth.dto.RegisterRequest;
import com.bearmq.api.tenant.TenantService;
import com.bearmq.api.tenant.dto.TenantInfo;
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
  private final AuthService authService;
  private final TenantService tenantService;


  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@RequestBody final RegisterRequest registerRequest) {
    final TenantInfo tenantInfo = tenantService.create(registerRequest);

    return ResponseEntity.ok(authService.authenticate(tenantInfo));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(final @RequestBody AuthRequest authRequest)  {
    return ResponseEntity.ok(authService.authenticate(authRequest));
  }
}
