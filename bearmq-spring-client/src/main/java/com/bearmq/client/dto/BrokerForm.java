package com.bearmq.client.dto;

import java.util.List;
import java.util.Map;

public record BrokerForm(
    String vhost,
    List<ExchangeForm> exchanges,
    List<QueueForm> queues,
    List<BindingForm> bindings) {
  public record ExchangeForm(
      String name,
      String type,
      boolean durable,
      boolean internal,
      boolean delayed,
      Map<String, Object> args) {}

  public record QueueForm(
      String name,
      boolean durable,
      boolean exclusive,
      boolean autoDelete,
      Map<String, Object> args) {}

  public record BindingForm(
      String source, String destination_type, String destination, String routingKey) {}
}
