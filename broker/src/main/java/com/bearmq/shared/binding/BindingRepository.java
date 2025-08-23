package com.bearmq.shared.binding;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BindingRepository extends JpaRepository<Binding, String> {
  List<Binding> findAllByVhostId(String vhostId);
}
