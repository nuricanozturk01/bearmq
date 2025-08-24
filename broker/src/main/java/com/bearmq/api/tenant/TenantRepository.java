package com.bearmq.api.tenant;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
  boolean existsByUsernameOrEmail(String username, String email);

  Optional<Tenant> findByApiKey(String apiKey);

  Optional<Tenant> findByUsername(String username);
}
