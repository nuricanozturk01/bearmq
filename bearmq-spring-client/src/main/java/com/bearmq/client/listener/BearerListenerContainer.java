package com.bearmq.client.listener;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.bearmq.client.BearMessagingTemplate;
import com.bearmq.client.config.BearConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BearerListenerContainer implements Closeable {
  public static final byte SPACE = 0x20;
  public static final byte HORIZONTAL_TAB = 0x09;
  public static final byte LINE_FEED = 0x0A;
  public static final byte CARRIAGE_RETURN = 0x0D;

  private static final Logger LOGGER = LoggerFactory.getLogger(BearerListenerContainer.class);
  private static final int SCHEDULED_THREAD_POOL_SIZE = 5;

  private final ObjectMapper objectMapper;
  private final BearConfig config;
  private final BearMessagingTemplate template;
  private final ScheduledExecutorService executor;
  private final Map<String, List<Handler>> handlersByQueue;

  public BearerListenerContainer(
      final ObjectMapper objectMapper,
      final BearMessagingTemplate template,
      final BearConfig config) {
    this.objectMapper = objectMapper;
    this.template = template;
    this.config = config;
    this.handlersByQueue = new ConcurrentHashMap<>();
    this.executor = Executors.newScheduledThreadPool(SCHEDULED_THREAD_POOL_SIZE);
  }

  public void register(final Map<String, List<Handler>> map) {
    handlersByQueue.putAll(map);
  }

  public void start() {
    for (final Map.Entry<String, List<Handler>> entry : handlersByQueue.entrySet()) {
      final String key = entry.getKey();
      final List<Handler> handlers = entry.getValue();

      final int initDelay = config.getInitialDelayMs();
      final int period = config.getPeriodMs();

      executor.scheduleWithFixedDelay(() -> run(key, handlers), initDelay, period, MILLISECONDS);
    }
  }

  private void run(final String queue, final List<Handler> handlers) {
    try {
      final Optional<byte[]> bytesOpt = template.receive(queue);
      if (bytesOpt.isEmpty()) {
        return;
      }

      final byte[] messageBody = bytesOpt.get();

      for (final Handler handler : handlers) {
        final Method method = handler.method();

        // No param. no need mapping. Actually no need listening. Only consume messages
        if (method.getParameterCount() == 0) {
          method.invoke(handler.bean());
          continue;
        }

        // The first parameter must be the message object.
        final Class<?> paramType = method.getParameterTypes()[0];
        mapAndInvoke(messageBody, paramType, method, handler);
      }
    } catch (final Exception e) {
      LOGGER.warn("Listener invoke failed for {}: {}", queue, e.toString());
    }
  }

  private void mapAndInvoke(
      final byte[] body, final Class<?> paramType, final Method method, final Handler handler)
      throws InvocationTargetException, IllegalAccessException {
    if (body == null) {
      return;
    }

    if (paramType.isPrimitive()) {
      throw new IllegalArgumentException("Primitive param unsupported: " + paramType);
    }

    final Object arg;
    if (paramType == byte[].class) {
      arg = body;
    } else if (isStringable(paramType)) {
      arg = new String(body, StandardCharsets.UTF_8);
    } else {
      if (!looksLikeJson(body)) {
        return;
      }
      try {
        arg = objectMapper.readValue(body, objectMapper.constructType(paramType));
      } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
        LOGGER.debug("JSON parse failed for {}: {}", paramType, e.getMessage());
        return;
      } catch (java.io.IOException e) {
        LOGGER.error("IO Exception for {}: {}", paramType, e.getMessage());
        throw new RuntimeException(e);
      }
    }

    method.invoke(handler.bean(), arg);
  }

  private boolean isStringable(final Class<?> t) {
    return t == Object.class || CharSequence.class.isAssignableFrom(t);
  }

  private boolean looksLikeJson(final byte[] body) {
    int i = 0;
    final int n = body.length;

    while (i < n) {
      final byte b = body[i];
      if (b == SPACE || b == HORIZONTAL_TAB || b == LINE_FEED || b == CARRIAGE_RETURN) {
        i++;
        continue;
      }
      return b == '{'
          || b == '['
          || b == '"'
          || b == '-'
          || (b >= '0' && b <= '9')
          || b == 't'
          || b == 'f'
          || b == 'n';
    }
    return false;
  }

  @Override
  public void close() {
    executor.shutdown();
  }
}
