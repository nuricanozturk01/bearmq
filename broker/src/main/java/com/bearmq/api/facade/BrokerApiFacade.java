package com.bearmq.api.facade;

import com.bearmq.api.tenant.dto.TenantInfo;
import com.bearmq.api.broker.dto.BrokerRequest;
import com.bearmq.server.broker.dto.VirtualHostInfo;
import com.bearmq.shared.binding.BindingService;
import com.bearmq.shared.exchange.Exchange;
import com.bearmq.shared.exchange.ExchangeService;
import com.bearmq.shared.queue.Queue;
import com.bearmq.shared.queue.QueueService;
import com.bearmq.shared.vhost.VirtualHostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrokerApiFacade {
  private final VirtualHostService virtualHostService;
  private final ExchangeService exchangeService;
  private final QueueService queueService;
  private final BindingService bindingService;

  @Transactional
  public void createBrokerObjects(final BrokerRequest request, final TenantInfo tenantInfo) {
    final var vhost = virtualHostService.findByTenantIdAndVhostName(tenantInfo.id(), request.vhost());

    List<Queue> queues = List.of();
    if (!request.queues().isEmpty()) {
      queues = queueService.createAll(vhost, request.queues());
    }

    List<Exchange> exchanges = List.of();
    if (!request.exchanges().isEmpty()) {
      exchanges = exchangeService.createAll(vhost, request.exchanges());
    }

    if (!request.bindings().isEmpty()) {
      bindingService.createAll(vhost, exchanges, queues, request.bindings());
    }
  }

  public VirtualHostInfo createVirtualHost(final TenantInfo tenantInfo) {
    return virtualHostService.create(tenantInfo);
  }

  public void deleteVirtualHost(final TenantInfo tenantInfo, final String vhostId) {
    virtualHostService.delete(tenantInfo, vhostId);
  }

  public Page<VirtualHostInfo> findAllByUserId(final TenantInfo tenantInfo, final Pageable pageable) {
    return virtualHostService.findAllByTenantId(tenantInfo.id(), pageable);
  }
}
