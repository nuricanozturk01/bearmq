package com.bearmq.demo;

import com.bearmq.client.BearTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSender {
  private final BearTemplate bearTemplate;

  public void send() {
    for (int i = 0; i < 1000; i++) {
      final var student = Student.builder()
              .address("Address" + i)
              .name("Name" + i)
              .age(i + 15)
              .build();

      bearTemplate.convertAndSend("exchangeA", "", student);
      log.info("Sending student " + i);
    }

    log.warn("Sent 10 student data to queueX and queueY");
  }
}
