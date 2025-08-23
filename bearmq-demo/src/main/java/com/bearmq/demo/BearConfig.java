package com.bearmq.demo;

import com.bearmq.client.model.BearBinding;
import com.bearmq.client.model.BearExchange;
import com.bearmq.client.model.BearQueue;
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
  BearExchange exchangeA() {
    return new BearExchange.Builder()
            .name("exchangeA")
            .type(BearExchange.Type.FANOUT)
            .durable(true)
            .build();
  }

  @Bean
  BearBinding bindA(final BearExchange exchangeA, final BearQueue queueX) {
    return new BearBinding.Builder()
            .exchange(exchangeA.name())
            .destination(queueX.name())
            .destinationType(BearBinding.DestinationType.QUEUE)
            .build();
  }

  @Bean
  BearBinding bindB(final BearExchange exchangeA, final BearQueue queueY) {
    return new BearBinding.Builder()
            .exchange(exchangeA.name())
            .destination(queueY.name())
            .destinationType(BearBinding.DestinationType.QUEUE)
            .build();
  }
}
