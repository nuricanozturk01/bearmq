package com.bearmq.shared.exchange;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, String> {
  Set<Exchange> findAllByVhostId(String vhostId);
}
