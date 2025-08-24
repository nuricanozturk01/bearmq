package com.bearmq.shared.binding;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BindingRepository extends JpaRepository<Binding, String> {
  List<Binding> findAllByVhostId(String vhostId);
}
