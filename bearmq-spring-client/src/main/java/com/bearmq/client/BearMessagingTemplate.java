package com.bearmq.client;

import com.bearmq.client.config.BearConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class BearMessagingTemplate implements BearTemplate {
  private static final int DEFAULT_CHUNK_SIZE = 1024 * 4;
  private static final long BATCH_BACKOFF_TIMEOUT = 1500L;
  private static final long BATCH_MULTIPLIER = 2;
  private static final Logger log = LoggerFactory.getLogger(BearMessagingTemplate.class);

  private final String host;
  private final int port;
  private final ObjectMapper objectMapper;

  public BearMessagingTemplate(final BearConfig bearConfig,
                               final ObjectMapper objectMapper) {
    this.host = bearConfig.getHost();
    this.port = bearConfig.getPort();
    this.objectMapper = objectMapper;
  }

  @Retryable(
          retryFor = IOException.class,
          backoff = @Backoff(value = BATCH_BACKOFF_TIMEOUT, multiplier = BATCH_MULTIPLIER),
          recover = "recover"
  )
  private void doSend(final Map<String, Object> frame) throws BearMQException {
    try (final var socket = new Socket(host, port);
         final var dos = new DataOutputStream(socket.getOutputStream())) {

      final byte[] bytes = objectMapper.writeValueAsBytes(frame);

      // send total bytes length
      dos.writeInt(bytes.length);

      int offset = 0;
      int chunk = 0;

      while (offset < bytes.length) {
        int chunkSize = Math.min(DEFAULT_CHUNK_SIZE, bytes.length - offset);
        dos.writeInt(++chunk);
        dos.write(bytes, offset, chunkSize);
        offset += chunkSize;
      }

      dos.flush();
    } catch (final IOException e) {
      throw new BearMQException("Could not send BearMQ message to " + host + ":" + port, e);
    }
  }


  @Recover
  public void recover(final IOException exception) {
    log.debug(exception.getMessage(), exception);
  }

  @Override
  public void send(final Message message) throws BearMQException {
    final Map<String, Object> frame = Map.of(
            "type", "PUBLISH_DEFAULT",
            "body", message.getBody()
    );

    doSend(frame);
  }

  @Override
  public void send(String routingKey, Message message) throws BearMQException {
    final Map<String, Object> frame = Map.of(
            "type", "PUBLISH_Q",
            "queue", routingKey,
            "body", message.getBody()
    );

    doSend(frame);
  }

  @Override
  public void send(final String exchange, final String routingKey, final Message message) throws BearMQException {
    final Map<String, Object> frame = Map.of(
            "type", "PUBLISH_Q",
            "queue", routingKey,
            "body", message.getBody()
    );

    doSend(frame);
  }

  @Override
  public void convertAndSend(final Object message) throws BearMQException {
    send(new Message(message.toString().getBytes(UTF_8)));
  }

  @Override
  public void convertAndSend(final String routingKey, final Object message) throws BearMQException {
    send(new Message(message.toString().getBytes(UTF_8)));
  }

  @Override
  public void convertAndSend(final String exchange, final String routingKey, final Object message) throws BearMQException {
    send(exchange, routingKey, new Message(message.toString().getBytes(UTF_8)));
  }
}
