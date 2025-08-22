package com.bearmq.broker.config;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

public class ThreadMetrics {
  private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
  private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();
  private static final OperatingSystemMXBean OPERATING_SYSTEM_MX_BEAN = ManagementFactory.getOperatingSystemMXBean();

  static {
    if (THREAD_MX_BEAN.isThreadCpuTimeSupported() && !THREAD_MX_BEAN.isThreadCpuTimeEnabled()) {
      THREAD_MX_BEAN.setThreadCpuTimeEnabled(true);
    }
  }

  public static void printUsage(Thread thread) {
    long id = thread.threadId();
    long cpuTimeNs = THREAD_MX_BEAN.getThreadCpuTime(id);
    long userTimeNs = THREAD_MX_BEAN.getThreadUserTime(id);

    MemoryUsage heap = MEMORY_MX_BEAN.getHeapMemoryUsage();
    MemoryUsage nonHeap = MEMORY_MX_BEAN.getNonHeapMemoryUsage();

    System.out.printf("Thread %s CPU=%.2f ms, User=%.2f ms | Heap=%.2f MB, NonHeap=%.2f MB OS:%s%n",
            thread.getName(),
            cpuTimeNs / 1_000_000.0,
            userTimeNs / 1_000_000.0,
            heap.getUsed() / (1024.0 * 1024),
            nonHeap.getUsed() / (1024.0 * 1024),
            OPERATING_SYSTEM_MX_BEAN.getName());
  }
}