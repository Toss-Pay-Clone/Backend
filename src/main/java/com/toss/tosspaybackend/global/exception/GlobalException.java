package com.toss.tosspaybackend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class GlobalException extends RuntimeException {
    private final ErrorCode errorCode;
    private String message;
}