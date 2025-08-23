package com.bearmq.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ThreadConfig {
  @Bean
  @Scope("prototype")
  @Primary
  public ExecutorService provideCachedThreadPool() {
    return Executors.newCachedThreadPool();
  }

  @Bean
  public ScheduledExecutorService provideMetricsScheduledThreadPool() {
    return Executors.newScheduledThreadPool(10);
  }
}
