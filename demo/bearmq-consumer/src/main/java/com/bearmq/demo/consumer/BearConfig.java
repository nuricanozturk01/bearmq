package com.bearmq.demo.consumer;

import com.bearmq.client.model.BearBinding;
import com.bearmq.client.model.BearExchange;
import com.bearmq.client.model.BearQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BearConfig {
  @Bean
  BearQueue queueZ() {
    return new BearQueue.Builder().name("queueZ").durable(true).build();
  }

  @Bean
  BearQueue queueT() {
    return new BearQueue.Builder().name("queueT").durable(true).build();
  }

  @Bean
  BearBinding bindExchangeAQueueZ() {
    return new BearBinding.Builder()
            .exchange("exchangeA")
            .destination("queueZ")
            .destinationType(BearBinding.DestinationType.QUEUE)
            .build();
  }

  @Bean
  BearBinding bindExchangeAQueueT() {
    return new BearBinding.Builder()
            .exchange("exchangeA")
            .destination("queueT")
            .destinationType(BearBinding.DestinationType.QUEUE)
            .build();
  }
}
