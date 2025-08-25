package com.bearmq.demo.consumer.second;

import com.bearmq.client.model.BearBinding;
import com.bearmq.client.model.BearExchange;
import com.bearmq.client.model.BearQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BearConfig {
  @Bean
  BearQueue queueX() {
    return new BearQueue.Builder().name("queueX").durable(true).build();
  }

  @Bean
  BearQueue queueY() {
    return new BearQueue.Builder().name("queueY").durable(true).build();
  }

  @Bean
  BearBinding bindExchangeBQueueX() {
    return new BearBinding.Builder()
            .exchange("exchangeB")
            .destination("queueX")
            .destinationType(BearBinding.DestinationType.QUEUE)
            .build();
  }

  @Bean
  BearBinding bindExchangeBQueueY() {
    return new BearBinding.Builder()
            .exchange("exchangeB")
            .destination("queueY")
            .destinationType(BearBinding.DestinationType.QUEUE)
            .build();
  }
}
