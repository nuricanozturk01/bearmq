package com.bearmq.demo;

import com.bearmq.client.listener.BearListener;
import org.springframework.stereotype.Service;

@Service
public class MessageListener {

  @BearListener(queues = "queueX")
  public void listenQueueX(final Student test) {

    System.out.println("MessageListener-X " + test);
  }

  //@BearListener(queues = "queueY")
  public void listenQueueY(final Student test) {
    System.out.println("MessageListener-Y " + test);
  }
}
