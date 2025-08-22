package com.bearmq.model;

import com.bearmq.broker.dto.BindRequest;
import com.bearmq.broker.dto.ExchangeRequest;
import com.bearmq.broker.dto.QueueRequest;
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
}
