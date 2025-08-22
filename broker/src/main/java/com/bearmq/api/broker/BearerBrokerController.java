package com.bearmq.api.broker;

import com.bearmq.api.auth.AuthComponent;
import com.bearmq.api.auth.AuthUtils;
import com.bearmq.api.tenant.TenantService;
import com.bearmq.api.tenant.dto.TenantInfo;
import com.bearmq.broker.dto.BrokerRequest;
import com.bearmq.broker.dto.VirtualHostInfo;
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
  private static final String API_KEY_HEADER = "X-API-KEY";

  private final BrokerApiFacade brokerApiFacade;
  private final AuthComponent authComponent;

  @PostMapping
  public ResponseEntity<Boolean> create(
          @RequestBody final BrokerRequest brokerRequest,
          @RequestHeader(HttpHeaders.AUTHORIZATION) final String bearerToken,
          @RequestHeader(API_KEY_HEADER) final String apiKey) {
    final String token = AuthUtils.checkBearerTokenValidityAndGet(bearerToken);

    final TenantInfo tenantInfo = authComponent.authorize(apiKey, token);

    brokerApiFacade.createBrokerObjects(brokerRequest, tenantInfo);

    return ResponseEntity.ok(true);
  }


  @PostMapping("/vhost")
  public ResponseEntity<VirtualHostInfo> createVhost(
          @RequestHeader(HttpHeaders.AUTHORIZATION) final String bearerToken,
          @RequestHeader(API_KEY_HEADER) final String apiKey) {
    final String token = AuthUtils.checkBearerTokenValidityAndGet(bearerToken);

    final TenantInfo tenantInfo = authComponent.authorize(apiKey, token);

    final VirtualHostInfo vhost = brokerApiFacade.createVirtualHost(tenantInfo);

    return ResponseEntity.ok(vhost);
  }
}
