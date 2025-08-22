package com.bearmq;

import com.bearmq.broker.BrokerServer;
import com.bearmq.broker.Constant;
import com.bearmq.metrics.MetricServer;
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
  private final MetricServer metricServer;

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

    if (isMetricEnabled) {
      Thread metricsThread = new Thread(metricServer::run, Constant.METRICS_THREAD_NAME);
      metricServer.getThreads().add(brokerThread);
      metricsThread.setDaemon(false);
      metricsThread.start();
    }
  }
}
