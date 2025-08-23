package com.bearmq.demo.consumer.second;

import com.bearmq.client.config.EnableBear;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBear
public class BearmqConsumerSecondApplication {

  public static void main(String[] args) {
    SpringApplication.run(BearmqConsumerSecondApplication.class, args);
  }

}
