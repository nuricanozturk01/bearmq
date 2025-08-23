package com.bearmq.shared.vhost;

import com.bearmq.shared.vhost.dto.VirtualHostInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VirtualHostConverter {
  VirtualHostInfo convert(VirtualHost virtualHost);
}
