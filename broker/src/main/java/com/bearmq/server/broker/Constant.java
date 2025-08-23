package com.bearmq.server.broker;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constant {
  public static final String SERVER_THREAD_POOL_BEAN_NAME = "com.bearmq.server.threadpool.fixed";
  public static final String BROKER_THREAD_NAME = "bearmq-broker-thread";
  public static final String METRICS_THREAD_NAME = "bearmq-metrics-thread";
}
