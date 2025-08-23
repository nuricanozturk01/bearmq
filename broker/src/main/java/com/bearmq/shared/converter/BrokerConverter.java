package com.bearmq.shared.converter;

import com.bearmq.api.broker.dto.BindRequest;
import com.bearmq.api.broker.dto.ExchangeRequest;
import com.bearmq.api.broker.dto.QueueRequest;
import com.bearmq.shared.binding.Binding;
import com.bearmq.shared.exchange.Exchange;
import com.bearmq.shared.queue.Queue;
import com.bearmq.shared.vhost.VirtualHost;
import com.bearmq.shared.vhost.dto.VirtualHostInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BrokerConverter {
  @Mapping(ignore = true, target = "arguments")
  Queue toQueue(QueueRequest queueRequest);

  @Mapping(ignore = true, target = "arguments")
  Binding toBinding(BindRequest bindRequest);

  @Mapping(ignore = true, target = "arguments")
  Exchange toExchange(ExchangeRequest exchangeRequest);

  VirtualHostInfo convert(VirtualHost virtualHost);
}
