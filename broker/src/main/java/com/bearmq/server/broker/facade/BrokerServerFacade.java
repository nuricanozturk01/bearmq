package com.bearmq.server.broker.facade;

import com.bearmq.api.tenant.TenantService;
import com.bearmq.api.tenant.dto.TenantInfo;
import com.bearmq.server.broker.dto.Auth;
import com.bearmq.server.broker.dto.Message;
import com.bearmq.shared.binding.Binding;
import com.bearmq.shared.binding.DestinationType;
import com.bearmq.shared.queue.Queue;
import com.bearmq.shared.vhost.VirtualHost;
import com.bearmq.shared.vhost.VirtualHostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrokerServerFacade {
  private final VirtualHostService virtualHostService;
  private final TenantService tenantService;
  private final ObjectMapper objectMapper;

  @Value("${bearmq.broker.storage-dir:./data/queues}")
  private String storageDir;

  private final Map<String, ChronicleQueue> queueCache = new ConcurrentHashMap<>();
  private final Map<String, List<String>> routes = new ConcurrentHashMap<>();
  private final Map<String, ExcerptTailer> consumerTailers = new ConcurrentHashMap<>();

  public void prepareAndUpQeueues(
          final VirtualHost vhost,
          final List<Queue> queues,
          final List<Binding> bindings) {
    for (final Queue q : queues)
      queueCache.computeIfAbsent(queueKey(vhost.getId(), q.getName()), k -> openChronicle(resolveQueuePath(q)));

    for (final Binding binding : bindings) {
      if (binding.getDestinationType() == DestinationType.EXCHANGE)
        continue;

      final String vhostId = vhost.getId();
      final String exchangeKey = exchangeKey(vhostId, binding.getSourceExchangeRef().getName());
      final String queueName = binding.getDestinationQueueRef().getName();

      routes.computeIfAbsent(exchangeKey, k -> new CopyOnWriteArrayList<>());
      final List<String> queueList = routes.get(exchangeKey);

      if (!queueList.contains(queueName))
        queueList.add(queueName);
    }

    log.debug("Queues opened: {}", queueCache.keySet());
    log.debug("Routes prepared: {}", routes.keySet());
  }

  public void identifyOperationAndApply(final Map<String, Object> mapObject) {
    final Message msg = objectMapper.convertValue(mapObject, Message.class);
    final VirtualHost vhost = getVhost(msg);

    switch (msg.getOperation()) {
      case "enqueue" -> enqueue(vhost, msg);
      case "publish" -> publish(vhost, msg);
      case "dequeue" -> dequeue(vhost, msg);
      case "dequeueAndRemove" -> dequeueAndRemove(vhost, msg);
      default -> throw new IllegalArgumentException("Unknown operation: " + msg.getOperation());
    }
  }

  private void dequeueAndRemove(final VirtualHost vhost, final Message msg) {
    dequeue(vhost, msg);
  }

  private void dequeue(final VirtualHost vhost, final Message msg) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private void enqueue(final VirtualHost vhost, final Message msg) {
    final String queueName = requireNonBlank(msg.getQueue(), "queue");
    final String key = queueKey(vhost.getId(), queueName);
    final ChronicleQueue cq = queueCache.get(key);
    if (cq == null) throw new IllegalArgumentException("Queue not found: " + queueName);

    final byte[] body = requireBody(msg);

    try (ExcerptAppender appender = cq.createAppender()) {
      appender.writeBytes(bytes -> bytes.write(body));
    }
  }

  private void publish(final VirtualHost vhost, final Message msg) {
    final String exName = requireNonBlank(msg.getExchange(), "exchange");

    final String exK = exchangeKey(vhost.getId(), exName);

    final List<String> queueNames = routes.getOrDefault(exK, List.of());

    if (queueNames.isEmpty()) {
      return;
    }

    final byte[] body = requireBody(msg);

    for (final String queueName : queueNames) {
      final ChronicleQueue cq = queueCache.get(queueKey(vhost.getId(), queueName));

      if (cq == null) {
        log.warn("Queue missing for route: {}", queueName);
        continue;
      }

      try (final ExcerptAppender appender = cq.createAppender()) {
        appender.writeBytes(bytes -> bytes.write(body));
      }
    }
  }

  private VirtualHost getVhost(final Message msg) {
    final Auth auth = msg.getAuth();
    final var apiKey = new String(Base64.getDecoder().decode(auth.getApiKey()), StandardCharsets.UTF_8);

    final TenantInfo tenantInfo = tenantService.findByApiKey(apiKey);
    final String host = new String(Base64.getDecoder().decode(auth.getVhost()), StandardCharsets.UTF_8);
    final String username = new String(Base64.getDecoder().decode(auth.getUsername()), StandardCharsets.UTF_8);

    return virtualHostService.findByVhostInfo(tenantInfo.id(), host, username, auth.getPassword());
  }

  private String decodeB64(String s) {
    return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
  }

  private String requireNonBlank(String v, String name) {
    if (v == null || v.isBlank()) throw new IllegalArgumentException("Missing field: " + name);
    return v;
  }

  private byte[] requireBody(Message m) {
    final byte[] b = m.getBody().getBytes(StandardCharsets.UTF_8);
    if (b.length == 0) {
      throw new IllegalArgumentException("Missing body");
    }
    return b;
  }


  private String queueKey(String vhostId, String qName) {
    return vhostId + ":" + qName;
  }

  private String exchangeKey(String vhostId, String exName) {
    return vhostId + ":" + exName;
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
  }
}
