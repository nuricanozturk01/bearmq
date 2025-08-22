package com.bearmq.client.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class BearQueue {
  private final String name;
  private final String actualName;
  private final boolean durable;
  private final boolean exclusive;
  private final boolean autoDelete;
  private final Map<String, Object> arguments;

  private BearQueue(Builder builder) {
    this.name = Objects.requireNonNull(builder.name, "name must not be null");
    this.actualName = builder.actualName;
    this.durable = builder.durable;
    this.exclusive = builder.exclusive;
    this.autoDelete = builder.autoDelete;
    this.arguments = builder.arguments != null ? builder.arguments : new HashMap<>();
  }

  public String name() {
    return name;
  }

  public String actualName() {
    return actualName;
  }

  public boolean durable() {
    return durable;
  }

  public boolean exclusive() {
    return exclusive;
  }

  public boolean autoDelete() {
    return autoDelete;
  }

  public Map<String, Object> arguments() {
    return arguments;
  }

  // ---- Builder ----
  public static class Builder {
    private String name;
    private String actualName;
    private boolean durable = true;
    private boolean exclusive = false;
    private boolean autoDelete = false;
    private Map<String, Object> arguments = new HashMap<>();

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder actualName(String actualName) {
      this.actualName = actualName;
      return this;
    }

    public Builder durable(boolean durable) {
      this.durable = durable;
      return this;
    }

    public Builder exclusive(boolean exclusive) {
      this.exclusive = exclusive;
      return this;
    }

    public Builder autoDelete(boolean autoDelete) {
      this.autoDelete = autoDelete;
      return this;
    }

    public Builder argument(String key, Object value) {
      this.arguments.put(key, value);
      return this;
    }

    public Builder arguments(Map<String, Object> arguments) {
      this.arguments = arguments;
      return this;
    }

    public BearQueue build() {
      return new BearQueue(this);
    }
  }
}