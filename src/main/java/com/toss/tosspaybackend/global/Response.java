package com.toss.tosspaybackend.global;

import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Builder
public record Response<T>(
        HttpStatusCode httpStatus,
        String message,
        T data
) {
}
