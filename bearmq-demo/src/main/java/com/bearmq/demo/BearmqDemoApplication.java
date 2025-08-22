package com.bearmq.demo;

import com.bearmq.client.config.EnableBear;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBear
@RequiredArgsConstructor
public class BearmqDemoApplication implements CommandLineRunner {
  private final Test test;
  public static void main(String[] args) {
    SpringApplication.run(BearmqDemoApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    test.send();
  }
}
