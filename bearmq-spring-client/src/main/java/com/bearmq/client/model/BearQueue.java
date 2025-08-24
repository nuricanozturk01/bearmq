package com.bearmq.client.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public final class BearQueue {
  private final String name;
  private final String actualName;
  private final boolean durable;
  private final boolean exclusive;
  private final boolean autoDelete;
  private final Map<String, Object> arguments;

  private BearQueue(final Builder builder) {
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

  public static class Builder {
    private String name;
    private String actualName;
    private boolean durable = true;
    private boolean exclusive = false;
    private boolean autoDelete = false;
    private Map<String, Object> arguments = new HashMap<>();

    public Builder name(final String name) {
      this.name = name;
      return this;
    }

    public Builder actualName(final String actualName) {
      this.actualName = actualName;
      return this;
    }

    public Builder durable(final boolean durable) {
      this.durable = durable;
      return this;
    }

    public Builder exclusive(final boolean exclusive) {
      this.exclusive = exclusive;
      return this;
    }

    public Builder autoDelete(final boolean autoDelete) {
      this.autoDelete = autoDelete;
      return this;
    }

    public Builder argument(final String key, final Object value) {
      this.arguments.put(key, value);
      return this;
    }

    public Builder arguments(final Map<String, Object> arguments) {
      this.arguments = arguments;
      return this;
    }

    public BearQueue build() {
      return new BearQueue(this);
    }
  }
}
