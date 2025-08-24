package com.bearmq.api.tenant;

import com.bearmq.api.subscription.SubscriptionPlan;
import com.bearmq.shared.vhost.VirtualHost;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "tenant")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Tenant {
  @Id
  @Column(nullable = false, length = 26, unique = true)
  private String id;

  @Column(name = "full_name")
  private String fullName;

  @Column(nullable = false, length = 150, unique = true)
  private String username;

  @Column(nullable = false, length = 150, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, length = 16)
  private String salt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @OneToOne
  @JoinColumn(name = "subscription_plan_id", nullable = false, referencedColumnName = "name")
  private SubscriptionPlan plan;

  @OneToMany(mappedBy = "tenant")
  private Set<VirtualHost> vhosts;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @ColumnDefault("ACTIVE")
  private TenantStatus status;

  @Column(nullable = false)
  private boolean deleted;

  @Column(name = "api_key", nullable = false, length = 52)
  private String apiKey;

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof Tenant tenant)) {
      return false;
    }

    return Objects.equals(id, tenant.id)
        && Objects.equals(username, tenant.username)
        && Objects.equals(email, tenant.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username, email);
  }
}
