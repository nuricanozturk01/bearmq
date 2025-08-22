package com.bearmq.broker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record QueueRequest(
        String name,
        boolean durable,
        boolean exclusive,

        @JsonProperty("auto_delete")
        boolean autoDelete,

        Map<String, Object> args
) {
}