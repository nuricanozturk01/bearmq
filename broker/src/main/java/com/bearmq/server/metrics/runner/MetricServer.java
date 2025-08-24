package com.bearmq.server.metrics.runner;

import com.bearmq.server.metrics.ThreadMetrics;
import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "bearmq.server.metrics.enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
@Data
public class MetricServer implements Closeable {
  private static final int INITIAL_DELAY_S = 3;
  private static final int MAX_DELAY_S = 5;

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
    scheduledPool.scheduleAtFixedRate(
        writeMetricsCallback, INITIAL_DELAY_S, MAX_DELAY_S, TimeUnit.SECONDS);
  }
}
