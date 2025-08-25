package com.bearmq.client;

import com.bearmq.client.dto.Message;
import java.util.Optional;

@SuppressWarnings("unused")
public interface BearTemplate {
  void send(String exchange, String routingKey, Message message) throws BearMQException;

  void send(String routingKey, Message message) throws BearMQException;

  void convertAndSend(String exchange, String routingKey, Object message) throws BearMQException;

  void convertAndSend(String routingKey, Object message) throws BearMQException;

  Optional<byte[]> receive(String queue) throws BearMQException;
}
