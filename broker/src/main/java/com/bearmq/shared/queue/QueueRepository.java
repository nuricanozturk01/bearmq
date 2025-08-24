package com.bearmq.shared.queue;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueRepository extends JpaRepository<Queue, String> {
  List<Queue> findAllByVhostId(String vhostId);
}
