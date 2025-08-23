package com.bearmq.shared.binding;

import com.bearmq.api.broker.dto.BindRequest;
import com.bearmq.shared.broker.Status;
import com.bearmq.shared.converter.BrokerConverter;
import com.bearmq.shared.exchange.Exchange;
import com.bearmq.shared.queue.Queue;
import com.bearmq.shared.vhost.VirtualHost;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BindingService {
  private final BindingRepository bindingRepository;
  private final BrokerConverter brokerConverter;

  public void createAll(VirtualHost vhost, List<Exchange> exchanges, List<Queue> queues, List<BindRequest> bindings) {
    final List<Pair<BindRequest, Binding>> bindingPairs = bindings.stream()
            .map(req -> Pair.of(req, brokerConverter.toBinding(req)))
            .toList();

    for (final var binding : bindingPairs) {
      binding.getSecond().setId(UlidCreator.getUlid().toString());
      binding.getSecond().setVhost(vhost);
      binding.getSecond().setStatus(Status.ACTIVE);
      binding.getSecond().setDestinationType(DestinationType.valueOf(binding.getFirst().destinationType()));
      binding.getSecond().setRoutingKey(binding.getFirst().routingKey());
      if (binding.getSecond().getDestinationType() == DestinationType.QUEUE && !queues.isEmpty()) {
        final var queue = queues.stream()
                .filter(q -> q.getName().equals(binding.getFirst().destination()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("queue is not found!"));

        binding.getSecond().setDestinationQueueRef(queue);
        binding.getSecond().setDestinationQueueId(queue.getId());
      } else {
        final var exchange = exchanges.stream()
                .filter(e -> e.getName().equals(binding.getFirst().destination()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("exchange is not found!"));
        binding.getSecond().setDestinationExchangeRef(exchange);
        binding.getSecond().setDestinationExchangeId(exchange.getId());
      }

      final Exchange sourceExchange = exchanges.stream()
              .filter(e -> e.getName().equals(binding.getFirst().source()))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("source is not found!"));
      binding.getSecond().setSourceExchangeId(sourceExchange.getId());
      binding.getSecond().setSourceExchangeRef(sourceExchange);
    }

    bindingRepository.saveAll(bindingPairs.stream().map(Pair::getSecond).toList());
  }
}
