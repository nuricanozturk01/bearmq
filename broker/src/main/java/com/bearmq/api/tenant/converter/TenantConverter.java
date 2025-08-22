package com.bearmq.api.tenant.converter;

import com.bearmq.api.tenant.Tenant;
import com.bearmq.api.tenant.dto.TenantInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TenantConverter {
  TenantInfo toTenantInfo(Tenant tenant);
}
