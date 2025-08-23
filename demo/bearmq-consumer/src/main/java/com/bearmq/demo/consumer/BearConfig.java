package com.bearmq.demo.consumer;

import com.bearmq.client.model.BearBinding;
import com.bearmq.client.model.BearExchange;
import com.bearmq.client.model.BearQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BearConfig {
  @Value("${app.queue}")
  private String queueName;

  @Value("${app.exchange}")
  private String exchangeName;

  @Bean
  BearQueue queueX() {
    return new BearQueue.Builder().name(queueName).durable(true).build();
  }

  @Bean
  BearBinding bindA(final BearQueue queueX) {
    return new BearBinding.Builder()
            .exchange(exchangeName)
            .destination(queueX.name())
            .destinationType(BearBinding.DestinationType.QUEUE)
            .build();
  }
}
