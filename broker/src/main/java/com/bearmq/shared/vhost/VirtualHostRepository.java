package com.bearmq.shared.vhost;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VirtualHostRepository extends JpaRepository<VirtualHost, String> {
  Optional<VirtualHost> findByTenantIdAndName(String tenantId, String name);

  Optional<VirtualHost> findByTenantIdAndId(String tenantId, String id);

  Page<VirtualHost> findAllByTenantId(String id, @NotNull Pageable pageable);

  Optional<VirtualHost> findByTenantIdAndNameAndUsernameAndPassword(
      String tenantId, String name, String username, String password);
}
