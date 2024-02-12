package com.toss.tosspaybackend.domain.member.dto;

import jakarta.validation.constraints.NotNull;

public record PasswordCheckRequest(
        @NotNull String password
) {
}
