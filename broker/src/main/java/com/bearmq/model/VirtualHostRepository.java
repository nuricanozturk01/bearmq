package com.bearmq.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VirtualHostRepository extends JpaRepository<VirtualHost, String> {
  Optional<VirtualHost> findByTenantIdAndName(String tenantId, String name);
}
