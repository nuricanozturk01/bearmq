package com.bearmq.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.bearmq.client.Names.CONFIG_BASE;

@ConfigurationProperties(CONFIG_BASE)
public class BearConfig {
  private String username;
  private String password;
  private String host;
  private int port;
  private String virtualHost;
  private String apiKey;
  private BearRetryConfig retry;

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getVirtualHost() {
    return virtualHost;
  }

  public void setVirtualHost(String virtualHost) {
    this.virtualHost = virtualHost;
  }

  public BearRetryConfig getRetry() {
    return retry;
  }

  public void setRetry(BearRetryConfig retry) {
    this.retry = retry;
  }

  public String getUrl() {
    if (host == null) {
      throw new IllegalStateException("host is null");
    } else if (host.contains("localhost") || host.contains("127.0.0.1") || host.contains("http")) {
      return String.format("http://%s:%d", host, 3333);
    }  else {
      return String.format("https://%s:%d", host, 3333);
    }
  }
}
