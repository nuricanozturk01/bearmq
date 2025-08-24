package com.bearmq.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AuthResponse(String token, @JsonProperty("refresh_token") String refreshToken) {}
