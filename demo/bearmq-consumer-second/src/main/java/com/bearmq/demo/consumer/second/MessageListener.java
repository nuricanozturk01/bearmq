package com.bearmq.demo.consumer.second;

import com.bearmq.client.listener.BearListener;
import org.springframework.stereotype.Service;

@Service
public class MessageListener {
  @BearListener(queues = "queueY")
  public void listenQueueY(final Student test) {
    System.out.println("MessageListener-Y " + test);
  }
}
