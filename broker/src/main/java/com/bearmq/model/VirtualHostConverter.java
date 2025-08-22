package com.bearmq.model;

import com.bearmq.broker.dto.VirtualHostInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VirtualHostConverter {
  VirtualHostInfo convert(VirtualHost virtualHost);
}
