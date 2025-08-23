package com.bearmq.shared.exchange;

import com.bearmq.api.broker.dto.ExchangeRequest;
import com.bearmq.shared.broker.Status;
import com.bearmq.shared.converter.BrokerConverter;
import com.bearmq.shared.vhost.VirtualHost;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

import static java.util.Locale.ROOT;
import static org.apache.commons.lang3.RandomStringUtils.secure;

@Service
@RequiredArgsConstructor
public class ExchangeService {
  private static final int MIN_DIGITS = 8;
  private static final int MAX_DIGITS = 30;
  private final ExchangeRepository exchangeRepository;
  private final BrokerConverter brokerConverter;
  private final Random random;


  public List<Exchange> createAll(final VirtualHost vhost, final List<ExchangeRequest> exchanges) {
    final var exchangeObjects = exchanges.stream()
            .map(brokerConverter::toExchange)
            .toList();

    for (final var exchange : exchangeObjects) {
      final String actualName = String.format("exchange-%s",
              secure().next(random.nextInt(MIN_DIGITS, MAX_DIGITS), true, false).toLowerCase(ROOT));

      exchange.setId(UlidCreator.getUlid().toString());
      exchange.setVhost(vhost);
      exchange.setActualName(actualName);
      exchange.setStatus(Status.ACTIVE);
    }

    return exchangeRepository.saveAll(exchangeObjects);
  }

}
