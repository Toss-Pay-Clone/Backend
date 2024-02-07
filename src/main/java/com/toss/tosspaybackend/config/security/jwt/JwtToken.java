package com.toss.tosspaybackend.config.security.jwt;

import lombok.Builder;

@Builder
public record JwtToken(
    String accessToken,
    String refreshToken,
    String grantType
) {
}
