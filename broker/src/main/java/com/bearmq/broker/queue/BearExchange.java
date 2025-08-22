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
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;


@Entity
@Table(name = "bear_exchange")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public final class BearExchange {
  @Id
  @Column(nullable = false, length = 26)
  private String id;

  @Column(nullable = false, length = 128)
  private String vhost;

  @Column(nullable = false)
  private String name;

  @Column(name = "actual_name", nullable = false)
  private String actualName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ExchangeType type;

  @Column(nullable = false)
  @ColumnDefault("true")
  private boolean durable;

  @Column(name = "auto_delete", nullable = false)
  @ColumnDefault("false")
  private boolean autoDelete;

  @Column(nullable = false)
  @ColumnDefault("false")
  private boolean internal;

  @Column(nullable = false)
  @ColumnDefault("false")
  private boolean delayed;

  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private JsonNode arguments;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @ColumnDefault("'ACTIVE'")
  private Status status;

  @Column(nullable = false)
  @ColumnDefault("0")
  private long version;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BearExchange e)) return false;
    return Objects.equals(id, e.id) && Objects.equals(actualName, e.actualName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, actualName);
  }
}