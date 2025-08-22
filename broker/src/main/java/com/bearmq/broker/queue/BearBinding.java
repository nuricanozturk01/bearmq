package com.bearmq.broker.queue;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "bear_binding",
        indexes = {
                @Index(name="ix_bind_vhost_src", columnList="vhost,source_exchange_id"),
                @Index(name="ix_bind_dst_q", columnList="destination_queue_id"),
                @Index(name="ix_bind_dst_ex", columnList="destination_exchange_id")
        }
)
@NoArgsConstructor @AllArgsConstructor
@Builder @Getter @Setter
public final class BearBinding {

  @Id
  @Column(nullable=false, length=26)
  private String id;

  @Column(nullable=false, length=128)
  private String vhost;

  @Column(name="source_exchange_id", nullable=false, length=26)
  private String sourceExchangeId;

  @Enumerated(EnumType.STRING)
  @Column(name="destination_type", nullable=false, length=16)
  private DestinationType destinationType;

  @Column(name="destination_queue_id", length=26)
  private String destinationQueueId;

  @Column(name="destination_exchange_id", length=26)
  private String destinationExchangeId;

  @Column(name="routing_key")
  private String routingKey;

  @Type(JsonType.class)
  @Column(columnDefinition="json")
  private JsonNode arguments;

  @Enumerated(EnumType.STRING)
  @Column(nullable=false, length=16)
  private Status status;

  @Column(nullable=false)
  private long version;

  @CreationTimestamp
  @Column(name="created_at", nullable=false, updatable=false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name="updated_at", nullable=false)
  private Instant updatedAt;

  @Override public boolean equals(Object o){
    if (!(o instanceof BearBinding b)) return false;
    return Objects.equals(id, b.id);
  }
  @Override public int hashCode(){ return Objects.hash(id); }
}