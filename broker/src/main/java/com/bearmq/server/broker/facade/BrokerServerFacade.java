package com.bearmq.server.broker.facade;

import com.bearmq.shared.binding.BindingService;
import com.bearmq.shared.exchange.ExchangeService;
import com.bearmq.shared.queue.QueueService;
import com.bearmq.shared.vhost.VirtualHostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrokerServerFacade {
  private final VirtualHostService virtualHostService;
  private final ExchangeService exchangeService;
  private final QueueService queueService;
  private final BindingService bindingService;



}
