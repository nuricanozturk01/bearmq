package com.bearmq.api.tenant.dto;

import com.bearmq.api.tenant.TenantStatus;

public record TenantInfo(
        String id,
        String fullName,
        String email,
        String username,
        TenantStatus status) {
}
