package com.bearmq.api.tenant;

import com.bearmq.api.auth.dto.RegisterRequest;
import com.bearmq.api.subscriptionplan.SubscriptionPlanRepository;
import com.bearmq.api.subscriptionplan.SubscriptionPlans;
import com.bearmq.api.tenant.converter.TenantConverter;
import com.bearmq.api.tenant.dto.TenantInfo;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantService {
  private final TenantRepository tenantRepository;
  private final TenantConverter tenantConverter;
  private final SubscriptionPlanRepository subscriptionPlanRepository;

  public TenantInfo create(final RegisterRequest request) {
    if (tenantRepository.existsByUsernameOrEmail(request.username(), request.email())) {
      return null;
    }

    final var plan = subscriptionPlanRepository.findByName((SubscriptionPlans.FREE))
            .orElseThrow(() -> new RuntimeException("Subscription Plan Not Found"));

    final Tenant tenantObj = Tenant.builder()
            .id(UlidCreator.getUlid().toString())
            .fullName(request.fullName())
            .email(request.email())
            .username(request.username())
            .plan(plan)
            .status(TenantStatus.ACTIVE)
            .apiKey(RandomStringUtils.secure().next(32, true, false))
            // password is not contains for easy dev process
            .build();

    final Tenant savedTenant = tenantRepository.save(tenantObj);

    return tenantConverter.toTenantInfo(savedTenant);
  }

  public TenantInfo findByUsername(final String username) {
    final Tenant tenant = tenantRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Tenant Not Found"));

    return tenantConverter.toTenantInfo(tenant);
  }

  public TenantInfo findByApiKey(String apiKey) {
    return tenantRepository.findByApiKey(apiKey)
            .map(tenantConverter::toTenantInfo)
            .orElseThrow(() -> new RuntimeException("Tenant Not Found"));
  }
}
