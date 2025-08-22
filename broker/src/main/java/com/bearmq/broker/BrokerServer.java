package com.bearmq.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Component
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("all")
public class BrokerServer implements Closeable {
  private final int DEFAULT_CUNK_SIZE = 1024 * 4;
  private final ServerSocket serverSocket;
  private final ExecutorService executorService;
  private final ObjectMapper objectMapper;

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
      log.error("Error accepting connection", e);
    }
  }

  private void handleClient(final Socket socket) {
    try {
      final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
      final int messageLength = dataInputStream.readInt();
      System.out.println("Received message: " + messageLength);
      if (messageLength <= 0 || messageLength > 16 * 16 * 1024) {
        log.error("Invalid message length: " + messageLength);
      }

      final var bytes = new byte[messageLength];

      int offset = 0;
      int expectedIdx = 1;

      while (offset < messageLength) {
        int chunkIdx = dataInputStream.readInt();
        log.debug("Received chunk: {}", chunkIdx);
        System.out.println("Received chunk: " + chunkIdx);
        if (chunkIdx != expectedIdx) {
          throw new IOException("chunk order mismatch");
        }

        int chunkLen = Math.min(DEFAULT_CUNK_SIZE, messageLength - offset);
        dataInputStream.readFully(bytes, offset, chunkLen);
        offset += chunkLen;
        expectedIdx++;
      }

      final Map<String, Object> oboj = objectMapper.readValue(bytes, Map.class);
      final var decodedBody = Base64.getDecoder().decode(oboj.get("body").toString());
      oboj.put("body", decodedBody);
      System.out.println(new String(bytes));
      String json = new String(decodedBody, java.nio.charset.StandardCharsets.UTF_8);
      System.out.println(json);
    } catch (final Exception e) {
      log.error("Error accepting connection", e);
    }
  }
}
