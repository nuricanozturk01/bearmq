package com.bearmq.client.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class BearExchange {

  public enum Type {DIRECT, FANOUT, TOPIC, HEADERS}

  private final String name;
  private final String actualName;
  private final Type type;
  private final boolean durable;
  private final boolean autoDelete;
  private final boolean internal;
  private final boolean delayed;
  private final Map<String, Object> arguments;

  private BearExchange(Builder b) {
    this.name = Objects.requireNonNull(b.name, "name");
    this.actualName = b.actualName;
    this.type = Objects.requireNonNull(b.type, "type");
    this.durable = b.durable;
    this.autoDelete = b.autoDelete;
    this.internal = b.internal;
    this.delayed = b.delayed;
    this.arguments = b.arguments != null ? b.arguments : new HashMap<>();
  }

  public String name() {
    return name;
  }

  public String actualName() {
    return actualName;
  }

  public Type type() {
    return type;
  }

  public boolean durable() {
    return durable;
  }

  public boolean autoDelete() {
    return autoDelete;
  }

  public boolean internal() {
    return internal;
  }

  public boolean delayed() {
    return delayed;
  }

  public Map<String, Object> arguments() {
    return arguments;
  }

  public static class Builder {
    private String name;
    private String actualName;
    private Type type = Type.DIRECT;
    private boolean durable = true;
    private boolean autoDelete = false;
    private boolean internal = false;
    private boolean delayed = false;
    private Map<String, Object> arguments = new HashMap<>();

    public Builder name(String v) {
      this.name = v;
      return this;
    }

    public Builder actualName(String v) {
      this.actualName = v;
      return this;
    }

    public Builder type(Type v) {
      this.type = v;
      return this;
    }

    public Builder durable(boolean v) {
      this.durable = v;
      return this;
    }

    public Builder autoDelete(boolean v) {
      this.autoDelete = v;
      return this;
    }

    public Builder internal(boolean v) {
      this.internal = v;
      return this;
    }

    public Builder delayed(boolean v) {
      this.delayed = v;
      return this;
    }

    public Builder argument(String k, Object v) {
      this.arguments.put(k, v);
      return this;
    }

    public Builder arguments(Map<String, Object> m) {
      this.arguments = m;
      return this;
    }

    public BearExchange build() {
      return new BearExchange(this);
    }
  }
}