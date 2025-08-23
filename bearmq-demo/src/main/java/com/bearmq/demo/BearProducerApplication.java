package com.bearmq.demo;

import com.bearmq.client.config.EnableBear;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableBear
@RequiredArgsConstructor
public class BearProducerApplication implements CommandLineRunner {
  private final MessageSender messageSender;
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  public static void main(String[] args) {
    SpringApplication.run(BearProducerApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    executor.scheduleAtFixedRate(messageSender::send, 0, 10, TimeUnit.SECONDS);
  }
}
