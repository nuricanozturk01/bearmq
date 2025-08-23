package com.bearmq.shared.queue;

import com.bearmq.api.broker.dto.QueueRequest;
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
public class QueueService {
  private static final int MIN_DIGITS = 8;
  private static final int MAX_DIGITS = 30;

  private final QueueRepository queueRepository;
  private final BrokerConverter brokerConverter;
  private final Random random;

  public List<Queue> createAll(final VirtualHost vhost, final List<QueueRequest> queues) {
    final var queueObjects = queues.stream()
            .map(brokerConverter::toQueue)
            .toList();

    for (final var queue : queueObjects) {
      final String actualName = String.format("queue-%s",
              secure().next(random.nextInt(MIN_DIGITS, MAX_DIGITS), true, false).toLowerCase(ROOT));
      queue.setId(UlidCreator.getUlid().toString());
      queue.setVhost(vhost);
      queue.setActualName(actualName);
      queue.setStatus(Status.ACTIVE);
      // for now
      queue.setOverflowPolicy(OverflowPolicy.DEAD_LETTER_QUEUE);
      queue.setMaxMessage(1024);
    }

    return queueRepository.saveAll(queueObjects);
  }

  public List<Queue> findAllByVhostId(String vhostId) {
    return queueRepository.findAllByVhostId(vhostId);
  }
}
