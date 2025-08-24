package com.bearmq.client.listener;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.bearmq.client.BearMessagingTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
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
  private static final int DELAY_S = 0;
  private static final int PERIOD = 500;

  private static final Logger LOGGER = LoggerFactory.getLogger(BearerListenerContainer.class);
  private static final int SCHEDULED_THREAD_POOL_SIZE = 4;

  private final ObjectMapper objectMapper;
  private final BearMessagingTemplate template;
  private final ScheduledExecutorService executor;
  private final Map<String, List<Handler>> handlersByQueue;

  public BearerListenerContainer(
      final ObjectMapper objectMapper, final BearMessagingTemplate template) {
    this.objectMapper = objectMapper;
    this.template = template;
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

      executor.scheduleWithFixedDelay(() -> run(key, handlers), DELAY_S, PERIOD, MILLISECONDS);
    }
  }

  private void run(final String queue, final List<Handler> handlers) {
    try {
      final Optional<byte[]> bytesOpt = template.receive(queue);
      if (bytesOpt.isEmpty()) {
        return;
      }

      final byte[] body = bytesOpt.get();

      for (final var handler : handlers) {
        final Method method = handler.method();
        if (method.getParameterCount() == 0) {
          method.invoke(handler.bean());
          continue;
        }

        final Class<?> parameterType = method.getParameterTypes()[0];

        try {
          final Object arg;
          if (parameterType == byte[].class) {
            arg = body;
          } else if (parameterType == String.class
              || parameterType == CharSequence.class
              || parameterType == Object.class) {
            arg = new String(body, StandardCharsets.UTF_8);
          } else {
            if (!looksLikeJson(body)) {
              continue;
            }
            arg = objectMapper.readValue(body, objectMapper.constructType(parameterType));
          }

          method.invoke(handler.bean(), arg);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ignored) {

        } catch (final Exception ex) {
          LOGGER.warn("Listener invoke failed for {}: {}", queue, ex.toString());
        }
      }
    } catch (final Exception e) {
      LOGGER.warn("Listener invoke failed for {}: {}", queue, e.toString());
    }
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
  public void close() throws IOException {
    executor.shutdown();
  }
}
