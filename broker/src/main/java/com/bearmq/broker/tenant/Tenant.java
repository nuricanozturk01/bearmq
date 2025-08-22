package com.bearmq.broker.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "tenant")
public class Tenant {
  @Id
  @Column(nullable = false, length = 26)
  private String id;

  @Column(name = "full_name")
  private String fullName;

  @Column(nullable = false, length = 150, unique = true)
  private String username;

  @Column(nullable = false, length = 150, unique = true)
  private String email;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
