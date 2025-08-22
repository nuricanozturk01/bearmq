package com.bearmq.api.broker;

import com.bearmq.api.auth.AuthService;
import com.bearmq.api.tenant.TenantService;
import com.bearmq.api.tenant.dto.TenantInfo;
import com.bearmq.broker.dto.BrokerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("api/broker")
@RestController
@RequiredArgsConstructor
public class BearerBrokerController {
  private final BrokerApiFacade brokerApiFacade;
  private final AuthService authService;
  private final TenantService tenantService;

  @PostMapping("/api-key")
  public ResponseEntity<Map<String, String>> createApiKey(@RequestHeader(HttpHeaders.AUTHORIZATION) final String authHeader) {
    final Pair<String, String> usernamePassword = authService.authorize(authHeader);
    final TenantInfo tenantInfo = tenantService.findByUsername(usernamePassword.getFirst());

    final String apiKey = brokerApiFacade.createApiKey(tenantInfo);

    return ResponseEntity.ok(Map.of("api_key", apiKey));
  }

  @PostMapping
  public ResponseEntity<Boolean> create(
          @RequestBody final BrokerRequest brokerRequest,
          @RequestHeader("X-API-KEY") final String apiKey) {

    return ResponseEntity.ok(true);
  }


}
