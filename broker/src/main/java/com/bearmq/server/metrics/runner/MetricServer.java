package com.bearmq.server.metrics.runner;

import com.bearmq.server.config.ThreadMetrics;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Data
public class MetricServer implements Closeable {
  private final DatagramSocket datagramSocket;
  private final List<Thread> threads = new ArrayList<>();
  private final ScheduledExecutorService scheduledPool;

  @Override
  public void close() throws IOException {
    scheduledPool.shutdown();
  }

  public void run() {
    log.warn("MetricServer started on port " + datagramSocket.getLocalPort());
    final Runnable writeMetricsCallback = () -> ThreadMetrics.printUsage(threads.getFirst());
    scheduledPool.scheduleAtFixedRate(writeMetricsCallback, 3, 5, TimeUnit.SECONDS);
  }
}
