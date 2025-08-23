package com.bearmq.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

@Configuration
@Lazy
public class SocketConfig {

  @Bean
  public ServerSocket provideServerSocket(
          @Value("${bearmq.server.broker.port}") final int port,
          @Value("${bearmq.server.broker.backlog}") final int backlog) throws IOException {
    return new ServerSocket(port, backlog);
  }


  @Bean
  public DatagramSocket provideDatagramSocket(
          @Value("${bearmq.server.metrics.port}") final int port
  ) throws IOException {
    return new DatagramSocket(port);
  }
}
