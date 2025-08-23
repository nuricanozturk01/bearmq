package com.bearmq.shared.binding;

import com.bearmq.api.broker.dto.BindRequest;
import com.bearmq.shared.broker.Status;
import com.bearmq.shared.converter.BrokerConverter;
import com.bearmq.shared.exchange.Exchange;
import com.bearmq.shared.queue.Queue;
import com.bearmq.shared.vhost.VirtualHost;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BindingService {
  private final BindingRepository bindingRepository;
  private final BrokerConverter brokerConverter;

  public List<Binding> createAll(
          final VirtualHost vhost,
          final List<Exchange> exchanges,
          final List<Queue> queues,
          final List<BindRequest> bindings) {
    final Map<String, Exchange> exchangeByName = exchanges.stream()
            .collect(Collectors.toMap(Exchange::getName, Function.identity()));

    final Map<String, Queue> queueByName = queues.stream()
            .collect(Collectors.toMap(Queue::getName, Function.identity()));

    final List<Binding> toPersist = new ArrayList<>(bindings.size());

    for (final BindRequest req : bindings) {
      final DestinationType destType = DestinationType.valueOf(req.destinationType());
      final Binding b = brokerConverter.toBinding(req);

      b.setId(UlidCreator.getUlid().toString());
      b.setVhost(vhost);
      b.setStatus(Status.ACTIVE);
      b.setDestinationType(destType);
      b.setRoutingKey(req.routingKey());

      setDestinations(destType, queueByName, req, exchangeByName, b);

      final Exchange sourceExchange = Optional.ofNullable(exchangeByName.get(req.source()))
              .orElseThrow(() -> new RuntimeException("source is not found!"));
      b.setSourceExchangeRef(sourceExchange);
      b.setSourceExchangeId(sourceExchange.getId());

      toPersist.add(b);
    }

    return bindingRepository.saveAll(toPersist);
  }

  private void setDestinations(
          final DestinationType destType,
          final Map<String, Queue> queueByName,
          final BindRequest req,
          final Map<String, Exchange> exchangeByName,
          final Binding b) {
    if (destType == DestinationType.QUEUE) {
      final Queue q = Optional.ofNullable(queueByName.get(req.destination()))
              .orElseThrow(() -> new RuntimeException("queue is not found!"));
      b.setDestinationQueueRef(q);
      b.setDestinationQueueId(q.getId());
    } else {
      final Exchange destEx = Optional.ofNullable(exchangeByName.get(req.destination()))
              .orElseThrow(() -> new RuntimeException("exchange is not found!"));
      b.setDestinationExchangeRef(destEx);
      b.setDestinationExchangeId(destEx.getId());
    }
  }
}
