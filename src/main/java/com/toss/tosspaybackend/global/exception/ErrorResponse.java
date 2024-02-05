package com.toss.tosspaybackend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class ErrorResponse {
    private final int httpStatus;
    private final String error;
    private final String message;

    public ErrorResponse(ErrorCode errorCode) {
        this.httpStatus = errorCode.getHttpStatus().value();
        this.error = errorCode.getHttpStatus().name();
        this.message = errorCode.getMessage();
    }

    public ErrorResponse(ErrorCode errorCode, String message) {
        this.httpStatus = errorCode.getHttpStatus().value();
        this.error = errorCode.getHttpStatus().name();
        this.message = message;
    }
}