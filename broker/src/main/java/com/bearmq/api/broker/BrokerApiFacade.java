package com.bearmq.api.broker;

import com.bearmq.api.tenant.Tenant;
import com.bearmq.api.tenant.TenantRepository;
import com.bearmq.api.tenant.dto.TenantInfo;
import com.bearmq.broker.dto.BindRequest;
import com.bearmq.broker.dto.BrokerRequest;
import com.bearmq.broker.dto.ExchangeRequest;
import com.bearmq.broker.dto.QueueRequest;
import com.bearmq.broker.dto.VirtualHostInfo;
import com.bearmq.model.Binding;
import com.bearmq.model.BindingRepository;
import com.bearmq.model.BrokerConverter;
import com.bearmq.model.DestinationType;
import com.bearmq.model.Exchange;
import com.bearmq.model.ExchangeRepository;
import com.bearmq.model.OverflowPolicy;
import com.bearmq.model.Queue;
import com.bearmq.model.QueueRepository;
import com.bearmq.model.Status;
import com.bearmq.model.VirtualHost;
import com.bearmq.model.VirtualHostConverter;
import com.bearmq.model.VirtualHostRepository;
import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import static java.lang.String.format;
import static java.util.Locale.ROOT;
import static org.apache.commons.lang3.RandomStringUtils.secure;

@Service
@RequiredArgsConstructor
public class BrokerApiFacade {
  private static final int MIN_DIGITS = 8;
  private static final int MAX_DIGITS = 30;

  private final TenantRepository tenantRepository;
  private final VirtualHostRepository virtualHostRepository;
  private final QueueRepository queueRepository;
  private final BindingRepository  bindingRepository;
  private final ExchangeRepository exchangeRepository;

  private final VirtualHostConverter virtualHostConverter;
  private final BrokerConverter brokerConverter;
  private final Random random;

  @Value("${bearmq.domain}")
  private String domain;

  @Transactional
  public void createBrokerObjects(final BrokerRequest request, final TenantInfo tenantInfo) {
    final var vhost = virtualHostRepository.findByTenantIdAndName(tenantInfo.id(), request.vhost())
            .orElseThrow(() -> new RuntimeException("vhost is not found!"));

    List<Queue> queues = List.of();
    if (!request.queues().isEmpty()) {
      queues = saveQueues(vhost, request.queues());
    }

    List<Exchange> exchanges = List.of();
    if (!request.exchanges().isEmpty()) {
      exchanges = saveExchanges(vhost, request.exchanges());
    }

    if (!request.bindings().isEmpty()) {
      saveBindings(vhost, exchanges, queues, request.bindings());
    }
  }

  private void saveBindings(VirtualHost vhost, List<Exchange> exchanges, List<Queue> queues, List<BindRequest> bindings) {
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

  private List<Exchange> saveExchanges(final VirtualHost vhost, final List<ExchangeRequest> exchanges) {
    final var exchangeObjects = exchanges.stream()
            .map(brokerConverter::toExchange)
            .toList();

    for (final var exchange : exchangeObjects) {
      exchange.setId(UlidCreator.getUlid().toString());
      exchange.setVhost(vhost);
      exchange.setActualName(secure().next(random.nextInt(MIN_DIGITS, MAX_DIGITS), true, false).toLowerCase(ROOT));
      exchange.setStatus(Status.ACTIVE);
    }

    return exchangeRepository.saveAll(exchangeObjects);
  }

  private List<Queue> saveQueues(final VirtualHost vhost, final List<QueueRequest> queues) {
    final var queueObjects = queues.stream()
            .map(brokerConverter::toQueue)
            .toList();

    for (final var queue : queueObjects) {
      queue.setId(UlidCreator.getUlid().toString());
      queue.setVhost(vhost);
      queue.setActualName(secure().next(random.nextInt(MIN_DIGITS, MAX_DIGITS), true, false).toLowerCase(ROOT));
      queue.setStatus(Status.ACTIVE);
      // for now
      queue.setOverflowPolicy(OverflowPolicy.DEAD_LETTER_QUEUE);
      queue.setMaxMessage(1024);
    }

    return queueRepository.saveAll(queueObjects);
  }

  @Transactional
  public VirtualHostInfo createVirtualHost(final TenantInfo tenantInfo) {
    final Tenant tenant = tenantRepository.findByUsername(tenantInfo.username())
            .orElseThrow(() -> new RuntimeException("Tenant Not Found"));

    final int randomDigit = random.nextInt(MIN_DIGITS, MAX_DIGITS);

    final var vhostDomain = String.format("%s.%s",
            secure().next(randomDigit, true, false).toLowerCase(ROOT), domain);

    final String username = secure()
            .next(randomDigit, true, false)
            .toLowerCase(ROOT);

    final String password = secure().next(randomDigit, true, false);
    final String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));

    final String name = format("%s-%s", tenantInfo.username(),
            secure().next(randomDigit, true, false).toLowerCase(ROOT));

    final var vhostObj = new VirtualHost();

    vhostObj.setName(name);
    vhostObj.setUsername(username);
    vhostObj.setPassword(encodedPassword);
    vhostObj.setDomain(vhostDomain);
    vhostObj.setUrl(vhostDomain);
    vhostObj.setId(UlidCreator.getUlid().toString());
    vhostObj.setTenant(tenant);
    vhostObj.setStatus(Status.ACTIVE);

    virtualHostRepository.save(vhostObj);

    return virtualHostConverter.convert(vhostObj);
  }
}
