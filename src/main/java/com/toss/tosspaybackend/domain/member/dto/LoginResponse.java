package com.toss.tosspaybackend.domain.member.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
    Long id,
    String name,
    String phone
) {
}
