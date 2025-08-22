package com.bearmq.demo;

import com.bearmq.client.model.BearBinding;
import com.bearmq.client.model.BearExchange;
import com.bearmq.client.model.BearQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BearConfig {

  @Bean
  BearQueue queueA() {
    return new BearQueue.Builder().name("queue-a").durable(true).build();
  }

  @Bean
  BearQueue queueB() {
    return new BearQueue.Builder().name("queue-b").durable(true).build();
  }

  @Bean
  BearExchange exchangeX() {
    return new BearExchange.Builder()
            .name("exchange-x")
            .type(BearExchange.Type.FANOUT)
            .durable(true)
            .build();
  }

  @Bean
  BearBinding bindA(BearExchange exchangeX, BearQueue queueA) {
    return new BearBinding.Builder()
            .exchange(exchangeX.name())
            .destination(queueA.name())
            .destinationType(BearBinding.DestinationType.QUEUE)
            .build();
  }

  @Bean
  BearBinding bindB(BearExchange exchangeX, BearQueue queueB) {
    return new BearBinding.Builder()
            .exchange(exchangeX.name())
            .destination(queueB.name())
            .destinationType(BearBinding.DestinationType.QUEUE)
            .build();
  }
}
