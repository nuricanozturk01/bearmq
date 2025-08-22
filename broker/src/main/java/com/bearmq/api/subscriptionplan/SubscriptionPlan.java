package com.bearmq.api.subscriptionplan;

import com.bearmq.api.tenant.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscription_plan")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SubscriptionPlan {
  @Id
  @Column(nullable = false, unique = true)
  @Enumerated(EnumType.STRING)
  private SubscriptionPlans name;

  @Column(name = "max_vhosts", nullable = false)
  private int maxVhosts;

  @Column(name = "max_queues", nullable = false)
  private int maxQueues;

  @Column(name = "max_exchange", nullable = false)
  private int maxExchange;

  @OneToOne(mappedBy = "plan")
  private Tenant tenant;

  @Column(nullable = false)
  private boolean deleted;
}
