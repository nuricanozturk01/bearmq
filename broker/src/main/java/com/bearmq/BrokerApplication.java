package com.bearmq;

import com.bearmq.server.broker.Constant;
import com.bearmq.server.broker.runner.BrokerServer;
import com.bearmq.server.metrics.runner.MetricServer;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
public class BrokerApplication implements ApplicationRunner {
  private final BrokerServer brokerServer;
  private final Optional<MetricServer> metricServer;

  @Value("${bearmq.server.metrics.enabled}")
  private boolean isMetricEnabled;

  public static void main(final String[] args) {
    SpringApplication.run(BrokerApplication.class, args);
  }

  @Override
  public void run(final ApplicationArguments args) {
    final Thread brokerThread = new Thread(brokerServer::run, Constant.BROKER_THREAD_NAME);
    brokerThread.setDaemon(false);
    brokerThread.start();

    brokerServer.loadCurrentQueues();

    if (isMetricEnabled && metricServer.isPresent()) {
      final Thread metricsThread =
          new Thread(metricServer.get()::run, Constant.METRICS_THREAD_NAME);
      metricServer.get().getThreads().add(brokerThread);
      metricsThread.setDaemon(false);
      metricsThread.start();
    }
  }
}
