package com.bearmq.shared.vhost;

import com.bearmq.api.tenant.Tenant;
import com.bearmq.api.tenant.TenantRepository;
import com.bearmq.api.tenant.dto.TenantInfo;
import com.bearmq.server.broker.dto.VirtualHostInfo;
import com.bearmq.shared.broker.Status;
import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

import static java.lang.String.format;
import static java.util.Locale.ROOT;
import static org.apache.commons.lang3.RandomStringUtils.secure;

@Service
@RequiredArgsConstructor
public class VirtualHostService {
  private static final int MIN_DIGITS = 8;
  private static final int MAX_DIGITS = 30;
  private final VirtualHostRepository repository;
  private final VirtualHostConverter converter;
  private final TenantRepository tenantRepository;
  private final Random random;

  @Value("${bearmq.domain}")
  private String domain;

  @Transactional
  public VirtualHostInfo create(final TenantInfo tenantInfo) {
    final Tenant tenant = tenantRepository.findByUsername(tenantInfo.username())
            .orElseThrow(() -> new RuntimeException("Tenant Not Found"));

    final int randomDigit = random.nextInt(MIN_DIGITS, MAX_DIGITS);

    final var vhostDomain = String.format("%s.%s",
            secure().next(randomDigit, true, false).toLowerCase(ROOT), domain);

    final String username = secure()
            .next(randomDigit, true, false)
            .toLowerCase(ROOT);

    final String password = secure().next(randomDigit, true, false);
    final String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));

    final String name = format("%s-%s", tenantInfo.username(),
            secure().next(randomDigit, true, false).toLowerCase(ROOT));

    final var vhostObj = new VirtualHost();

    vhostObj.setName(name);
    vhostObj.setUsername(username);
    vhostObj.setPassword(encodedPassword);
    vhostObj.setDomain(vhostDomain);
    vhostObj.setUrl(vhostDomain);
    vhostObj.setId(UlidCreator.getUlid().toString());
    vhostObj.setTenant(tenant);
    vhostObj.setStatus(Status.ACTIVE);

    repository.save(vhostObj);

    return converter.convert(vhostObj);
  }

  @Transactional
  public void delete(final TenantInfo tenantInfo, final String vhostId) {
    final var vhost = repository.findByTenantIdAndId(tenantInfo.id(), vhostId)
            .orElseThrow(() -> new RuntimeException("vhost is not found!"));
    repository.delete(vhost);
  }

  @Transactional(readOnly = true)
  public Page<VirtualHostInfo> findAllByTenantId(String id, @NotNull Pageable pageable) {
    final var vhosts = repository.findAllByTenantId(id, pageable)
            .stream()
            .map(converter::convert)
            .toList();

    return new PageImpl<>(vhosts, pageable, vhosts.size());
  }

  @Transactional(readOnly = true)
  public VirtualHost findByTenantIdAndVhostName(String id, String vhost) {
    return repository.findByTenantIdAndName(id, vhost)
            .orElseThrow(() -> new RuntimeException("vhost is not found!"));
  }
}
