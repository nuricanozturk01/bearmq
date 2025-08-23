package com.bearmq.demo.consumer;

import com.bearmq.client.listener.BearListener;
import org.springframework.stereotype.Service;

@Service
public class MessageListener {
  @BearListener(queues = "queueX")
  public void listenQueueX(final Student test) {
    System.out.println("MessageListener-X " + test);
  }
}
