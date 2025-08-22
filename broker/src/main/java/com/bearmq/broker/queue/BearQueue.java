package com.bearmq.broker.queue;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "bear_queue")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public final class BearQueue {
  @Id
  @Column(nullable = false, length = 26)
  private String id;

  @Column(nullable = false, length = 26)
  private String name;

  @Column(name = "actual_name", nullable = false, unique = true)
  private String actualName;

  @Column(name = "vhost", nullable = false, unique = true)
  private String vhost;

  @Column(nullable = false)
  @ColumnDefault("true")
  private boolean durable;

  @Column(nullable = false)
  @ColumnDefault("false")
  private boolean exclusive;

  @Column(name = "auto_delete", nullable = false)
  @ColumnDefault("false")
  private boolean autoDelete;

  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private JsonNode arguments;

  @Enumerated(EnumType.STRING)
  @Column
  private Status status;

  @Column(name = "overflow_policy", nullable = false)
  @Enumerated(EnumType.STRING)
  @ColumnDefault("BLOCK")
  private OverflowPolicy overflowPolicy;

  @Column(name = "max_bytes", nullable = false)
  @ColumnDefault("4096")
  private long maxBytes;

  @Column(name = "max_message_count", nullable = false)
  private long maxMessage;

  @Column(name = "message_ttl_ms")
  private long messageTtlMs;

  @Column(name = "message_retention_ms")
  private long messageRetentionMs;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BearQueue bearQueue)) {
      return false;
    }

    return Objects.equals(id, bearQueue.id) && Objects.equals(actualName, bearQueue.actualName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, actualName);
  }
}