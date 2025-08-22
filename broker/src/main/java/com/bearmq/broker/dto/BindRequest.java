package com.bearmq.broker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BindRequest(
        String source,
        @JsonProperty("destination_type")
        String destinationType,

        String destination,

        @JsonProperty("routing_key")
        String routingKey
) {
}
