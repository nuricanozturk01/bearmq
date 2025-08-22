package com.bearmq.api.broker;

import com.bearmq.api.tenant.TenantRepository;
import com.bearmq.api.tenant.dto.TenantInfo;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrokerApiFacade {
  private final TenantRepository tenantRepository;

  // Template for simulate flow
  public String createApiKey(final TenantInfo tenantInfo) {
    final var t = UlidCreator.getUlid().toLowerCase() + UlidCreator.getUlid().toString();

    final var apiKey = Arrays.stream(t.split(""))
            .collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
              Collections.shuffle(l);
              return l.stream();
            }))
            .collect(Collectors.joining());


    final var tenant = tenantRepository.findByUsername(tenantInfo.username())
            .orElseThrow(() -> new RuntimeException("Tenant Not Found"));

    tenant.setApiKey(apiKey);
    tenantRepository.save(tenant);
    return apiKey;
  }
}
