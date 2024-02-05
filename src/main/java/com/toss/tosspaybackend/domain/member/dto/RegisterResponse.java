package com.toss.tosspaybackend.domain.member.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RegisterResponse(
    Long id,
    String name,
    LocalDateTime createdAt
) {
}
