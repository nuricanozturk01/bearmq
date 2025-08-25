package com.bearmq.demo;

import com.bearmq.client.model.BearBinding;
import com.bearmq.client.model.BearExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BearConfig {
  @Bean
  BearExchange exchangeA() {
    return new BearExchange.Builder()
            .name("exchangeA")
            .type(BearExchange.Type.FANOUT)
            .durable(true)
            .build();
  }

  @Bean
  BearExchange exchangeB() {
    return new BearExchange.Builder()
            .name("exchangeB")
            .type(BearExchange.Type.FANOUT)
            .durable(true)
            .build();
  }

  @Bean
  BearBinding bindingExchangeAToB(
          @Qualifier("exchangeA") final BearExchange exchangeA,
          @Qualifier("exchangeB") final BearExchange exchangeB) {
    return new BearBinding.Builder()
            .exchange(exchangeA.name())
            .destination(exchangeB.name())
            .destinationType(BearBinding.DestinationType.EXCHANGE)
            .build();
  }
}
