package com.bearmq.demo;

import com.bearmq.client.BearTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSender {
  private final BearTemplate bearTemplate;
  private final Random random = new Random();

  @Async
  public void send() {
    final var student = Student.builder()
            .address("Address-" + random.nextInt(10, 10000))
            .name("Name" + random.nextInt(10, 10000))
            .age(+random.nextInt(10, 30))
            .build();

    bearTemplate.convertAndSend("exchangeB", "", student);
    log.warn("Sent message: {}", student);
  }
}
