package com.bearmq.broker.dto;

import java.util.List;

public record BrokerRequest(
        String vhost,
        int schemaVersion,
        List<ExchangeRequest> exchanges,
        List<QueueRequest> queues,
        List<BindRequest> bindings
) {}