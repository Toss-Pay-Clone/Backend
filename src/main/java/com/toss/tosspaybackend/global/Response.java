package com.toss.tosspaybackend.global;

import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record Response<T>(
        HttpStatus httpStatus,
        String message,
        T data
) {
}
