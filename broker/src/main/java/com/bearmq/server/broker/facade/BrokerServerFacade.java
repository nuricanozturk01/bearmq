package com.bearmq.server.broker.facade;

import com.bearmq.api.tenant.TenantService;
import com.bearmq.api.tenant.dto.TenantInfo;
import com.bearmq.server.broker.dto.Auth;
import com.bearmq.server.broker.dto.Message;
import com.bearmq.shared.binding.Binding;
import com.bearmq.shared.binding.BindingService;
import com.bearmq.shared.binding.DestinationType;
import com.bearmq.shared.queue.Queue;
import com.bearmq.shared.queue.QueueService;
import com.bearmq.shared.vhost.VirtualHost;
import com.bearmq.shared.vhost.VirtualHostService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("resource")
public class BrokerServerFacade {
  private final VirtualHostService virtualHostService;
  private final QueueService queueService;
  private final BindingService bindingService;
  private final TenantService tenantService;

  @Qualifier("thread.virtual")
  private final ExecutorService virtualThreadPool;

  @Value("${bearmq.broker.storage-dir:./data/queues}")
  private String storageDir;

  private final Map<String, ChronicleQueue> queueCache = new ConcurrentHashMap<>();
  private final Map<String, Set<String>> routes = new ConcurrentHashMap<>();
  private final Map<String, ExcerptTailer> consumerTailers = new ConcurrentHashMap<>();
  private final Map<String, ReentrantLock> queueLocks = new ConcurrentHashMap<>();

  public void prepareAndUpQueues(final VirtualHost vhost, final List<Queue> queues, final List<Binding> bindings) {
    for (final Queue queue : queues) {
      final String key = queueKey(vhost.getId(), queue.getName());

      queueCache.computeIfAbsent(key, k -> openChronicle(resolveQueuePath(queue)));
      queueLocks.computeIfAbsent(key, k -> new ReentrantLock(true));
    }

    for (final Binding binding : bindings) {
      if (binding.getDestinationType() == DestinationType.EXCHANGE) {
        // Implement Later
        continue;
      }

      final String vhostId = vhost.getId();
      final String exchangeKey = exchangeKey(vhostId, binding.getSourceExchangeRef().getName());
      final String queueName = binding.getDestinationQueueRef().getName();

      routes.computeIfAbsent(exchangeKey, k -> ConcurrentHashMap.newKeySet()).add(queueName);
    }

    log.warn("Queues opened: {}", queueCache.keySet());
    log.warn("Routes prepared: {}", routes.keySet());
  }

  public Optional<byte[]> identifyOperationAndApply(final Message msg) {
    final VirtualHost vhost = getVhost(msg);

    return switch (msg.getOperation()) {
      case "enqueue" -> {
        enqueue(vhost, msg);
        yield Optional.empty();
      }
      case "publish" -> {
        publish(vhost, msg);
        yield Optional.empty();
      }
      case "dequeue" -> dequeue(vhost, msg);
      default -> throw new IllegalArgumentException("Unknown operation: " + msg.getOperation());
    };
  }

  public void loadQueues() {
    final var vhosts = virtualHostService.findAll();

    for (final VirtualHost vhost : vhosts) {
      final var queues = queueService.findAllByVhostId(vhost.getId());
      final var bindings = bindingService.findAllByVhostId(vhost.getId());

      prepareAndUpQueues(vhost, queues, bindings);
    }
  }

  private Optional<byte[]> dequeue(final VirtualHost vhost, final Message msg) {
    final String queueName = msg.getQueue();
    final String key = queueKey(vhost.getId(), queueName);

    final ChronicleQueue chronicleQueue = queueCache.get(key);
    if (chronicleQueue == null) {
      return Optional.empty();
    }

    final ReentrantLock queueLock = queueLocks.get(key);
    if (queueLock == null) {
      return Optional.empty();
    }

    // NOTE: Tailers are NOT thread-safe, sharing a Tailer between threads will lead to errors and unpredictable behaviour.
    final ExcerptTailer tailer = consumerTailers.computeIfAbsent(key, k -> chronicleQueue.createTailer().toStart());

    Future<Optional<byte[]>> future = virtualThreadPool.submit(() -> lock(queueLock, tailer));

    try {
      return future.get(500, java.util.concurrent.TimeUnit.MILLISECONDS);
    } catch (final TimeoutException e) {
      Thread.currentThread().interrupt();
      return Optional.empty();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Optional<byte[]> lock(final ReentrantLock lock, final ExcerptTailer tailer) {
    lock.lock();
    try {
      final AtomicReference<byte[]> responseBody = new AtomicReference<>();

      final boolean ok = tailer.readBytes(in -> {
        final byte[] buf = new byte[(int) in.readRemaining()];
        in.read(buf);
        responseBody.set(buf);
      });

      return ok && responseBody.get() != null ? Optional.of(responseBody.get()) : Optional.empty();
    } finally {
      lock.unlock();
    }
  }

  private void enqueue(final VirtualHost vhost, final Message msg) {
    final String queueName = msg.getQueue();
    final String key = queueKey(vhost.getId(), queueName);
    final ChronicleQueue chronicleQueue = queueCache.get(key);

    if (chronicleQueue == null) {
      throw new IllegalArgumentException("Queue not found: " + queueName);
    }

    final byte[] body = msg.getBody();
    if (body.length == 0) {
      throw new IllegalArgumentException("Missing body");
    }

    try (final ExcerptAppender appender = chronicleQueue.createAppender()) {
      appender.writeBytes(bytes -> bytes.write(body));
    }
  }

  private void publish(final VirtualHost vhost, final Message msg) {
    final String exchangeName = msg.getExchange();
    final String key = exchangeKey(vhost.getId(), exchangeName);

    final Set<String> queueNames = routes.getOrDefault(key, Set.of());
    if (queueNames.isEmpty()) {
      return;
    }

    final byte[] body = msg.getBody();
    if (body.length == 0) {
      throw new IllegalArgumentException("Missing body");
    }

    for (final String queueName : queueNames) {
      final ChronicleQueue chronicleQueue = queueCache.get(queueKey(vhost.getId(), queueName));

      if (chronicleQueue == null) {
        log.warn("Queue missing for route: {}", queueName);
        continue;
      }

      try (final ExcerptAppender appender = chronicleQueue.createAppender()) {
        appender.writeBytes(bytes -> bytes.write(body));
      }
    }
  }

  private VirtualHost getVhost(final Message msg) {
    final Auth auth = msg.getAuth();

    // Security will fix later
    final TenantInfo tenantInfo = tenantService.findByApiKey(decodeBase64(auth.getApiKey()));

    final String host = decodeBase64(auth.getVhost());
    final String username = decodeBase64(auth.getUsername());

    return virtualHostService.findByVhostInfo(tenantInfo.id(), host, username, auth.getPassword());
  }

  private String queueKey(String vhostId, String queueName) {
    return String.format("%s:%s", vhostId, queueName);
  }

  private String exchangeKey(String vhostId, String exchangeName) {
    return String.format("%s:%s", vhostId, exchangeName);
  }

  private ChronicleQueue openChronicle(Path dir) {
    try {
      Files.createDirectories(dir);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return ChronicleQueue.singleBuilder(dir.toFile()).build();
  }

  private Path resolveQueuePath(Queue q) {
    return Path.of(storageDir + File.separator + q.getVhost().getId() + File.separator + q.getName());
  }

  private String decodeBase64(final String val) {
    return new String(Base64.getDecoder().decode(val), StandardCharsets.UTF_8);
  }

  @PreDestroy
  public void shutdown() {
    queueCache.values().forEach(cq -> {
      try {
        cq.close();
      } catch (Throwable ignore) {
      }
    });
    consumerTailers.clear();
    routes.clear();
    queueCache.clear();
    virtualThreadPool.shutdown();
  }
}
