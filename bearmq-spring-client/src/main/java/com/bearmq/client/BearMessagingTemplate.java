package com.bearmq.client;

import com.bearmq.client.config.BearConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class BearMessagingTemplate implements BearTemplate {
  private static final Logger LOGGER = LoggerFactory.getLogger(BearMessagingTemplate.class);

  private static final int DEFAULT_CHUNK_SIZE = 1024 * 4;
  private static final long BATCH_BACKOFF_TIMEOUT = 1500L;
  private static final long BATCH_MULTIPLIER = 2;

  private final ObjectMapper objectMapper;
  private final BearConfig bearConfig;

  public BearMessagingTemplate(final BearConfig bearConfig, final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.bearConfig = bearConfig;
  }

  private void sendBytes(final DataOutputStream dos, final byte[] bytes) throws IOException {
    dos.writeInt(bytes.length);

    int offset = 0, chunk = 0;
    while (offset < bytes.length) {
      final int chunkSize = Math.min(DEFAULT_CHUNK_SIZE, bytes.length - offset);
      dos.writeInt(++chunk);
      dos.write(bytes, offset, chunkSize);
      offset += chunkSize;
    }
    dos.flush();
  }

  private Optional<byte[]> readChunkedResponse(final DataInputStream dis) throws IOException {
    final int totalLen = dis.readInt();

    if (totalLen <= 0) {
      return Optional.empty();
    }

    final byte[] buf = new byte[totalLen];

    int off = 0;
    while (off < totalLen) {
      final int chunkIdx = dis.readInt();
      final int chunkLen = Math.min(DEFAULT_CHUNK_SIZE, totalLen - off);
      dis.readFully(buf, off, chunkLen);
      off += chunkLen;
    }
    return Optional.of(buf);
  }

  @Recover
  public void recover(final IOException exception) {
    LOGGER.debug(exception.getMessage(), exception);
  }

  @Override
  public void send(final String routingKey, final Message message) throws BearMQException {
    final Map<String, Object> frame =
        Map.of(
            "operation", "enqueue", "queue", routingKey, "auth", getAuth(), "body", message.body());
    doSend(frame);
  }

  @Override
  public void send(final String exchange, final String routingKey, final Message message)
      throws BearMQException {
    final Map<String, Object> frame =
        Map.of(
            "operation",
            "publish",
            "exchange",
            exchange,
            "routingKey",
            routingKey,
            "auth",
            getAuth(),
            "body",
            message.body());
    doSend(frame);
  }

  @Override
  public void convertAndSend(final String routingKey, final Object message) throws BearMQException {
    try {
      final byte[] body = objectMapper.writeValueAsBytes(message);
      send(routingKey, new Message(body));
    } catch (final Exception e) {
      throw new BearMQException("Serialization failed", e);
    }
  }

  @Override
  public void convertAndSend(final String exchange, final String routingKey, final Object message)
      throws BearMQException {
    try {
      final byte[] body = objectMapper.writeValueAsBytes(message);
      send(exchange, routingKey, new Message(body));
    } catch (final Exception e) {
      throw new BearMQException("Serialization failed", e);
    }
  }

  public Optional<byte[]> receive(final String queue) throws BearMQException {
    final Map<String, Object> frame =
        Map.of("operation", "dequeue", "queue", queue, "auth", getAuth());
    return doReceive(frame);
  }

  private Optional<byte[]> doReceive(final Map<String, Object> frame) throws BearMQException {
    try (final Socket socket = new Socket(bearConfig.getHost(), bearConfig.getPort());
        final DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        final DataInputStream dis = new DataInputStream(socket.getInputStream())) {
      final byte[] bytes = objectMapper.writeValueAsBytes(frame);

      sendBytes(dos, bytes);

      return readChunkedResponse(dis);
    } catch (final IOException e) {
      throw new BearMQException("request failed", e);
    }
  }

  @Retryable(
      retryFor = IOException.class,
      backoff = @Backoff(value = BATCH_BACKOFF_TIMEOUT, multiplier = BATCH_MULTIPLIER),
      recover = "recover")
  public void doSend(final Map<String, Object> frame) throws BearMQException {
    try (final Socket socket = new Socket(bearConfig.getHost(), bearConfig.getPort());
        final DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
      final byte[] bytes = objectMapper.writeValueAsBytes(frame);
      sendBytes(dos, bytes);
    } catch (final IOException e) {
      throw new BearMQException(
          "Could not send BearMQ message to " + bearConfig.getHost() + ":" + bearConfig.getPort(),
          e);
    }
  }

  private Map<String, Object> getAuth() {
    return Map.of(
        "vhost", Base64.getEncoder().encodeToString(bearConfig.getVirtualHost().getBytes()),
        "username", Base64.getEncoder().encodeToString(bearConfig.getUsername().getBytes()),
        "password", Base64.getEncoder().encodeToString(bearConfig.getPassword().getBytes()),
        "apiKey", Base64.getEncoder().encodeToString(bearConfig.getApiKey().getBytes()));
  }
}
