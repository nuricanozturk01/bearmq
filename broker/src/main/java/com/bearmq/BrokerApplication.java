package com.bearmq;

import com.bearmq.api.subscription.SubscriptionPlan;
import com.bearmq.api.subscription.SubscriptionPlanRepository;
import com.bearmq.api.subscription.SubscriptionPlans;
import com.bearmq.server.broker.runner.BrokerServer;
import com.bearmq.server.broker.Constant;
import com.bearmq.server.metrics.runner.MetricServer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
public class BrokerApplication implements ApplicationRunner {
  private final BrokerServer brokerServer;
  private final Optional<MetricServer> metricServer;
  private final SubscriptionPlanRepository subscriptionPlanRepository;

  @Value("${bearmq.server.metrics.enabled}")
  private boolean isMetricEnabled;

  public static void main(String[] args) {
    SpringApplication.run(BrokerApplication.class, args);
  }

  @Override
  public void run(final ApplicationArguments args) {
    Thread brokerThread = new Thread(brokerServer::run, Constant.BROKER_THREAD_NAME);
    brokerThread.setDaemon(false);
    brokerThread.start();

    brokerServer.loadCurrentQueues();

    if (isMetricEnabled && metricServer.isPresent()) {
      Thread metricsThread = new Thread(metricServer.get()::run, Constant.METRICS_THREAD_NAME);
      metricServer.get().getThreads().add(brokerThread);
      metricsThread.setDaemon(false);
      metricsThread.start();
    }

    persistSubscriptionPlans();
  }

  private void persistSubscriptionPlans() {
    final var plan = subscriptionPlanRepository.findByName((SubscriptionPlans.FREE));

    if (plan.isPresent()) {
      return;
    }

    final SubscriptionPlan free = SubscriptionPlan.builder()
            .name(SubscriptionPlans.FREE)
            .maxExchange(3)
            .maxQueues(10)
            .maxVhosts(3)
            .build();

    final SubscriptionPlan pro = SubscriptionPlan.builder()
            .name(SubscriptionPlans.PRO)
            .maxExchange(5)
            .maxQueues(20)
            .maxVhosts(5)
            .build();

    final SubscriptionPlan enterprise = SubscriptionPlan.builder()
            .name(SubscriptionPlans.ENTERPRISE)
            .maxExchange(10)
            .maxQueues(30)
            .maxVhosts(10)
            .build();

    subscriptionPlanRepository.saveAll(List.of(free, pro, enterprise));
  }
}
