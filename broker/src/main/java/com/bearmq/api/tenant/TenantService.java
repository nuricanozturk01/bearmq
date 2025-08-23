package com.bearmq.api.tenant;

import com.bearmq.api.auth.dto.RegisterRequest;
import com.bearmq.api.subscription.SubscriptionPlanRepository;
import com.bearmq.api.subscription.SubscriptionPlans;
import com.bearmq.api.tenant.converter.TenantConverter;
import com.bearmq.api.tenant.dto.TenantAuthenticateInfo;
import com.bearmq.api.tenant.dto.TenantInfo;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantService {
  private static final int SALT_LENGTH = 16;
  private static final int API_KEY_LENGTH = 52;

  private final TenantRepository tenantRepository;
  private final TenantConverter tenantConverter;
  private final SubscriptionPlanRepository subscriptionPlanRepository;

  public TenantAuthenticateInfo create(final RegisterRequest request) {
    if (tenantRepository.existsByUsernameOrEmail(request.username(), request.email())) {
      return null;
    }

    final var plan = subscriptionPlanRepository.findByName((SubscriptionPlans.FREE))
            .orElseThrow(() -> new RuntimeException("Subscription Plan Not Found"));

    final var salt = RandomStringUtils.secure().nextAlphanumeric(SALT_LENGTH);
    final var password = DigestUtils.sha256Hex(salt + request.password());

    final var apiKey = String.format("bearmqt-%s",
            RandomStringUtils.secure().next(API_KEY_LENGTH, true, false));

    final Tenant tenantObj = Tenant.builder()
            .id(UlidCreator.getUlid().toString())
            .fullName(request.fullName())
            .email(request.email())
            .username(request.username())
            .plan(plan)
            .status(TenantStatus.ACTIVE)
            .apiKey(apiKey)
            .salt(salt)
            .password(password)
            .build();

    final Tenant savedTenant = tenantRepository.save(tenantObj);

    return tenantConverter.toTenantAuthenticateInfo(savedTenant);
  }

  public TenantAuthenticateInfo findByUsername(final String username) {
    final Tenant tenant = tenantRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Tenant Not Found"));

    return tenantConverter.toTenantAuthenticateInfo(tenant);
  }

  public TenantInfo findByApiKey(String apiKey) {
    return tenantRepository.findByApiKey(apiKey)
            .map(tenantConverter::toTenantInfo)
            .orElseThrow(() -> new RuntimeException("Tenant Not Found"));
  }
}
