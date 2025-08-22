package com.bearmq.client.config;

import com.bearmq.client.Names;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(Names.BEAR_RETRY_CONFIG)
public class BearRetryConfig {
  private boolean enabled;
  private String initialInterval;
  private String maxInterval;
  private int maxAttempts;
  private int multiplier;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getInitialInterval() {
    return initialInterval;
  }

  public void setInitialInterval(String initialInterval) {
    this.initialInterval = initialInterval;
  }

  public String getMaxInterval() {
    return maxInterval;
  }

  public void setMaxInterval(String maxInterval) {
    this.maxInterval = maxInterval;
  }

  public int getMaxAttempts() {
    return maxAttempts;
  }

  public void setMaxAttempts(int maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  public int getMultiplier() {
    return multiplier;
  }

  public void setMultiplier(int multiplier) {
    this.multiplier = multiplier;
  }
}
