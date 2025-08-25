package com.bearmq.server.broker.facade;

import static com.bearmq.shared.binding.DestinationType.EXCHANGE;
import static java.util.concurrent.ConcurrentHashMap.newKeySet;

import com.bearmq.api.tenant.TenantService;
import com.bearmq.api.tenant.dto.TenantInfo;
import com.bearmq.server.broker.dto.Auth;
import com.bearmq.server.broker.dto.Message;
import com.bearmq.shared.binding.Binding;
import com.bearmq.shared.binding.BindingService;
import com.bearmq.shared.queue.Queue;
import com.bearmq.shared.queue.QueueService;
import com.bearmq.shared.vhost.VirtualHost;
import com.bearmq.shared.vhost.VirtualHostService;
import jakarta.annotation.PreDestroy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("resource")
public class BrokerServerFacade {
  private static final int THREAD_WAIT_MS = 500;
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
  private final Map<String, Set<String>> exchangeToExchanges = new ConcurrentHashMap<>();

  public void loadQueues() {
    final List<VirtualHost> vhosts = virtualHostService.findAll();

    for (final VirtualHost vhost : vhosts) {
      final List<Queue> queues = queueService.findAllByVhostId(vhost.getId());
      final List<Binding> bindings = bindingService.findAllByVhostId(vhost.getId());

      prepareAndUpQueues(vhost, queues, bindings);
    }
  }

  public void prepareAndUpQueues(
    final VirtualHost vhost, final List<Queue> queues, final List<Binding> bindings) {
    final String vhostId = vhost.getId();

    prepareQueues(vhost.getId(), queues);

    for (final Binding binding : bindings) {
      final String sourceExchangeName = binding.getSourceExchangeRef().getName();
      final String sourceExchangeKey = exchangeKey(vhostId, sourceExchangeName);

      if (binding.getDestinationType() == EXCHANGE) {
        final String destinationExchangeName = binding.getDestinationExchangeRef().getName();
        bindExchangeToExchange(vhostId, destinationExchangeName, sourceExchangeKey);
        continue;
      }

      final String queueName = binding.getDestinationQueueRef().getName();
      routes.computeIfAbsent(sourceExchangeKey, k -> newKeySet()).add(queueName);
    }

    log.info("Queues opened: {}", queueCache.keySet());
    log.info("Routes prepared: {}", routes.keySet());
  }

  public Optional<byte[]> identifyOperationAndApply(final Message msg) {
    final VirtualHost vhost = getVhost(msg);

    return switch (msg.getOperation()) {
      case ENQUEUE -> enqueue(vhost, msg);
      case PUBLISH -> publish(vhost, msg);
      case DEQUEUE -> dequeue(vhost, msg);
    };
  }

  private Optional<byte[]> enqueue(final VirtualHost vhost, final Message msg) {
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

    return Optional.empty();
  }

  private Optional<byte[]> publish(final VirtualHost vhost, final Message msg) {
    final String exchangeName = msg.getExchange();
    final String key = exchangeKey(vhost.getId(), exchangeName);

    final Set<String> queueNames = resolveQueuesFor(key);

    if (queueNames.isEmpty()) {
      return Optional.empty();
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

    return Optional.empty();
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

    // NOTE: Tailers are NOT thread-safe, sharing a Tailer between threads will lead to errors and
    // unpredictable behaviour.
    final ExcerptTailer tailer =
      consumerTailers.computeIfAbsent(key, k -> chronicleQueue.createTailer().toStart());

    final Future<Optional<byte[]>> future = virtualThreadPool.submit(() -> readInQueue(queueLock, tailer));

    try {
      return future.get(THREAD_WAIT_MS, java.util.concurrent.TimeUnit.MILLISECONDS);
    } catch (final TimeoutException e) {
      future.cancel(true);
      return Optional.empty();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Optional<byte[]> readInQueue(final ReentrantLock lock, final ExcerptTailer tailer) {
    lock.lock();
    try {
      final AtomicReference<byte[]> responseBody = new AtomicReference<>();

      final boolean ok =
        tailer.readBytes(
          in -> {
            final byte[] buf = new byte[(int) in.readRemaining()];
            in.read(buf);
            responseBody.set(buf);
          });

      return ok && responseBody.get() != null ? Optional.of(responseBody.get()) : Optional.empty();
    } finally {
      lock.unlock();
    }
  }

  private Set<String> resolveQueuesFor(final String rootExKey) {
    final Set<String> visitedExchanges = ConcurrentHashMap.newKeySet();
    final Set<String> resultQueues = ConcurrentHashMap.newKeySet();
    final ArrayDeque<String> q = new ArrayDeque<>();
    q.add(rootExKey);

    while (!q.isEmpty()) {
      final String ex = q.poll();

      if (!visitedExchanges.add(ex)) {
        continue;
      }

      // ex → queues
      final Set<String> qs = routes.getOrDefault(ex, Set.of());
      resultQueues.addAll(qs);

      // ex → exchanges
      final Set<String> next = exchangeToExchanges.getOrDefault(ex, Set.of());
      for (final String nx : next) {
        if (!visitedExchanges.contains(nx)) {
          q.add(nx);
        }
      }
    }
    return resultQueues;
  }

  private void prepareQueues(final String vhostId, final List<Queue> queues) {
    for (final Queue queue : queues) {
      final String key = queueKey(vhostId, queue.getName());

      queueCache.computeIfAbsent(key, k -> openChronicle(resolveQueuePath(queue)));
      queueLocks.computeIfAbsent(key, k -> new ReentrantLock(true));
    }
  }

  private void bindExchangeToExchange(final String vhostId, final String destExchangeName, final String srcExchangeKey) {
    final String destinationExchangeKey = exchangeKey(vhostId, destExchangeName);

    if (!isCreateCycle(srcExchangeKey, destinationExchangeKey)) {
      exchangeToExchanges
        .computeIfAbsent(srcExchangeKey, k -> newKeySet())
        .add(destinationExchangeKey);
    }
  }

  private boolean isCreateCycle(final String srcExchangeKey, final String destExchangeKey) {
    if (srcExchangeKey.equals(destExchangeKey)) {
      return true;
    }
    final Set<String> visited = ConcurrentHashMap.newKeySet();
    final ArrayDeque<String> queue = new ArrayDeque<>();
    queue.add(destExchangeKey);

    // BFS
    while (!queue.isEmpty()) {
      final String exchangeKey = queue.poll();
      if (!visited.add(exchangeKey)) {
        continue;
      }

      if (exchangeKey.equals(srcExchangeKey)) {
        return true;
      }

      final Set<String> nextExchanges = exchangeToExchanges.getOrDefault(exchangeKey, Set.of());
      queue.addAll(nextExchanges);
    }

    return false;
  }

  private ChronicleQueue openChronicle(final Path dir) {
    try {
      Files.createDirectories(dir);
    } catch (final Exception e) {
      log.error("Failed to create chronicle queue.", e);
      throw new IllegalStateException(e);
    }

    return ChronicleQueue.singleBuilder(dir.toFile()).build();
  }

  private VirtualHost getVhost(final Message msg) {
    final Auth auth = msg.getAuth();

    // Security will fix later
    final TenantInfo tenantInfo = tenantService.findByApiKey(decodeBase64(auth.getApiKey()));

    final String host = decodeBase64(auth.getVhost());
    final String username = decodeBase64(auth.getUsername());

    return virtualHostService.findByVhostInfo(tenantInfo.id(), host, username, auth.getPassword());
  }

  private String decodeBase64(final String val) {
    return new String(Base64.getDecoder().decode(val), StandardCharsets.UTF_8);
  }

  private Path resolveQueuePath(final Queue q) {
    return Path.of(
      storageDir + File.separator + q.getVhost().getId() + File.separator + q.getName());
  }

  private String queueKey(final String vhostId, final String queueName) {
    return String.format("%s:%s", vhostId, queueName);
  }

  private String exchangeKey(final String vhostId, final String exchangeName) {
    return String.format("%s:%s", vhostId, exchangeName);
  }

  @PreDestroy
  public void shutdown() {
    queueCache
      .values()
      .forEach(
        cq -> {
          try {
            cq.close();
          } catch (Throwable ignore) {
          }
        });
    consumerTailers.clear();
    routes.clear();
    exchangeToExchanges.clear();
    queueCache.clear();
    virtualThreadPool.shutdown();
  }
}
