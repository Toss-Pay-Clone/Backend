package com.toss.tosspaybackend.config.security.jwt;

import com.toss.tosspaybackend.config.security.jwt.enums.TokenStatus;
import lombok.Builder;
import org.springframework.security.core.Authentication;

@Builder
public record TokenAuthentication(
    Authentication authentication,
    TokenStatus tokenStatus
) {
}
