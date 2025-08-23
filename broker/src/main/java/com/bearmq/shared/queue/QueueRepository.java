package com.bearmq.shared.queue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueueRepository extends JpaRepository<Queue, String> {
  List<Queue> findAllByVhostId(String vhostId);
}
