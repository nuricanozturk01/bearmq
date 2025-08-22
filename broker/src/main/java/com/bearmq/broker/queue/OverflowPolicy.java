package com.bearmq.broker.queue;

public enum OverflowPolicy {
  DEAD_LETTER_QUEUE,
  BLOCK,
  REJECT
}
