package com.bearmq.client.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class BearBinding {
  private final String exchange;
  private final String destination;
  private final DestinationType destinationType;
  private final String routingKey;
  private final Map<String, Object> arguments;
  private final boolean noWait;
  private final BearQueue lazyQueue;

  public enum DestinationType { QUEUE, EXCHANGE }

  private BearBinding(Builder b) {
    this.exchange = Objects.requireNonNull(b.exchange, "exchange");
    this.destination = b.destination;
    this.destinationType = Objects.requireNonNull(b.destinationType, "destinationType");
    this.routingKey = b.routingKey;
    this.arguments = b.arguments != null ? b.arguments : Collections.emptyMap();
    this.noWait = b.noWait;
    this.lazyQueue = b.lazyQueue;
  }

  public String getExchange() { return exchange; }
  public String getDestination() { return lazyQueue != null ? lazyQueue.name() : destination; }
  public DestinationType getDestinationType() { return destinationType; }
  public String getRoutingKey() {
    if (routingKey == null && lazyQueue != null) return lazyQueue.name();
    return routingKey;
  }
  public Map<String, Object> getArguments() { return arguments; }
  public boolean isNoWait() { return noWait; }
  public boolean isDestinationQueue() { return DestinationType.QUEUE.equals(destinationType); }

  public static class Builder {
    private String exchange;
    private String destination;
    private DestinationType destinationType;
    private String routingKey;
    private Map<String, Object> arguments = new HashMap<>();
    private boolean noWait = false;
    private BearQueue lazyQueue;

    public Builder exchange(String exchange) { this.exchange = exchange; return this; }
    public Builder destination(String destination) { this.destination = destination; return this; }
    public Builder destinationType(DestinationType type) { this.destinationType = type; return this; }
    public Builder routingKey(String key) { this.routingKey = key; return this; }
    public Builder argument(String k, Object v) { this.arguments.put(k, v); return this; }
    public Builder arguments(Map<String, Object> args) { this.arguments = args; return this; }
    public Builder noWait(boolean nw) { this.noWait = nw; return this; }
    public Builder lazyQueue(BearQueue q) { this.lazyQueue = q; return this; }

    public BearBinding build() { return new BearBinding(this); }
  }
}