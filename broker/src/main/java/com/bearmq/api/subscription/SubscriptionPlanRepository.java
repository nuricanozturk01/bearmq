package com.bearmq.api.subscription;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
  Optional<SubscriptionPlan> findByName(SubscriptionPlans name);
}
