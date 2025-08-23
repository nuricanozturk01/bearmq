package com.bearmq.server.metrics;

import lombok.Data;

@Data
public abstract class BearMetric {
  private final String metricName;
}
