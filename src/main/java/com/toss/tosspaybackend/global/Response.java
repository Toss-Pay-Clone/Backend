package com.toss.tosspaybackend.global;

import lombok.Builder;

@Builder
public record Response<T>(
        int status,
        String message,
        T data
) {
}
