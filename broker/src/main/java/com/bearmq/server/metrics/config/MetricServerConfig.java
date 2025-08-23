package com.bearmq.server.metrics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class MetricServerConfig {
  @Bean
  @ConditionalOnProperty(value = "bearmq.server.metrics.enabled", havingValue = "true")
  public ScheduledExecutorService provideMetricsScheduledThreadPool() {
    return Executors.newScheduledThreadPool(10);
  }

  @Bean
  @ConditionalOnProperty(value = "bearmq.server.metrics.enabled", havingValue = "true")
  public DatagramSocket provideDatagramSocket(@Value("${bearmq.server.metrics.port}") final int port) throws IOException {
    return new DatagramSocket(port);
  }
}
