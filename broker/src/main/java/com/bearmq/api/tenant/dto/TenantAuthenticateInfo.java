package com.bearmq.api.tenant.dto;

import com.bearmq.api.tenant.TenantStatus;

public record TenantAuthenticateInfo(
        String id,
        String fullName,
        String email,
        String username,
        String password,
        String salt,
        TenantStatus status) {
}
