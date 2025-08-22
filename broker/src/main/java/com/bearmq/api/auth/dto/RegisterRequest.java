package com.bearmq.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotEmpty
        @Size(min = 5, max = 150)
        @JsonProperty("full_name")
        String fullName,

        @NotEmpty
        @Size(min = 5, max = 150)
        String username,

        @NotEmpty
        @Size(min = 5, max = 150)
        @Email
        String email,

        @NotEmpty
        @Size(min = 5, max = 50)
        String password) {
}
