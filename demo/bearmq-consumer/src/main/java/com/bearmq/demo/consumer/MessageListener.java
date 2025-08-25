package com.bearmq.demo.consumer;

import com.bearmq.client.listener.BearListener;
import org.springframework.stereotype.Service;

@Service
public class MessageListener {
  @BearListener(queues = "queueZ")
  public void listenQueueZ(final Student test) {
    System.out.println("MessageListener-Z (A): " + test);
  }

  @BearListener(queues = "queueT")
  public void listenQueueT(final Student test) {
    System.out.println("MessageListener-T (A): " + test);
  }
}
