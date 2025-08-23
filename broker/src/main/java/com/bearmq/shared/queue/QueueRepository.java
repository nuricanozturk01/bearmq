package com.bearmq.shared.queue;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueRepository extends JpaRepository<Queue, String> {
}
