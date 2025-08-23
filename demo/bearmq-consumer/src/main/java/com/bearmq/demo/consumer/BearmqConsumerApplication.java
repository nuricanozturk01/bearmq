package com.bearmq.demo.consumer;

import com.bearmq.client.config.EnableBear;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBear
public class BearmqConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(BearmqConsumerApplication.class, args);
  }

}
