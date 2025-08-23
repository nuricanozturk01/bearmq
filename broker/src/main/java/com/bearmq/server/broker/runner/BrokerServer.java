package com.bearmq.server.broker.runner;

import com.bearmq.server.broker.facade.BrokerServerFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Component
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("all")
public class BrokerServer implements Closeable {
  private static final int DEFAULT_CUNK_SIZE = 1024 * 4;
  private static final int MAX_MESSAGE_SIZE = 16 * 16 * 1024;

  private final ServerSocket serverSocket;
  private final ExecutorService executorService;
  private final ObjectMapper objectMapper;
  private final BrokerServerFacade brokerFacade;

  @PreDestroy
  public void destroy() throws IOException {
    serverSocket.close();
    executorService.shutdown();
  }

  @Override
  public void close() throws IOException {
    serverSocket.close();
  }

  public void run() {
    try {
      log.warn("Broker server started on port " + serverSocket.getLocalPort());

      while (true) {
        final var socket = serverSocket.accept();
        executorService.execute(() -> handleClient(socket));
      }
    } catch (final IOException e) {
      Thread.currentThread().interrupt();
      log.error("Error accepting connection", e);
    }
  }

  private void handleClient(final Socket socket) {
    try (final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {
      final int messageLength = dataInputStream.readInt();

      if (messageLength <= 0 || messageLength > MAX_MESSAGE_SIZE) {
        log.error("Invalid message length: " + messageLength);
      }

      final var bytes = new byte[messageLength];
      int offset = 0;
      int expectedIdx = 1;
      while (offset < messageLength) {
        int chunkIdx = dataInputStream.readInt();

        if (chunkIdx != expectedIdx) {
          throw new IOException("chunk order mismatch");
        }

        int chunkLen = Math.min(DEFAULT_CUNK_SIZE, messageLength - offset);

        dataInputStream.readFully(bytes, offset, chunkLen);

        offset += chunkLen;
        expectedIdx++;
      }

      final Map<String, Object> mapObject = objectMapper.readValue(bytes, Map.class);
      log.warn("Received data from client: {}", mapObject);

      brokerFacade.identifyOperationAndApply(mapObject);
    } catch (final Exception e) {
      Thread.currentThread().interrupt();
      log.error("Error accepting connection", e);
    }
  }
}
