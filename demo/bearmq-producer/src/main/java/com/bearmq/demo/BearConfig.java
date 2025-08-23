package com.bearmq.demo;

import com.bearmq.client.model.BearExchange;
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
}
