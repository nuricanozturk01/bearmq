package com.bearmq.shared.queue;

public enum OverflowPolicy {
  DEAD_LETTER_QUEUE,
  BLOCK,
  REJECT
}
