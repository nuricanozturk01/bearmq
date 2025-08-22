package com.bearmq.client;

public interface BearTemplate {
  void send(String exchange, String routingKey, Message message) throws BearMQException;
  void send(String routingKey, Message message) throws BearMQException;
  void send(Message message) throws BearMQException;
  void convertAndSend(String exchange, String routingKey, Object message) throws BearMQException;
  void convertAndSend(String routingKey, Object message) throws BearMQException;
  void convertAndSend(Object message) throws BearMQException;
}