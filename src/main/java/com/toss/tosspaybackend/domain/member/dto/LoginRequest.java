package com.toss.tosspaybackend.domain.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LoginRequest(
        @NotNull String password
) {
}
