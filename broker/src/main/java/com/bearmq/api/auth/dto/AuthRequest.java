package com.bearmq.api.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotEmpty
        @Size(min = 3, max = 150)
        String username,

        @NotEmpty
        @Size(min = 3, max = 255)
        String password) {
}
