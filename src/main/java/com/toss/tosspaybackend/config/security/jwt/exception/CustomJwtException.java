package com.toss.tosspaybackend.config.security.jwt.exception;

import com.toss.tosspaybackend.config.security.jwt.enums.TokenType;
import io.jsonwebtoken.JwtException;
import lombok.Getter;

@Getter
public class CustomJwtException extends JwtException {
    private final TokenType tokenType;

    public CustomJwtException(TokenType tokenType, String message) {
        super(message);
        this.tokenType = tokenType;
    }

    public CustomJwtException(TokenType tokenType, Throwable cause) {
        super(cause.getMessage(), cause);
        this.tokenType = tokenType;
    }

    public CustomJwtException(TokenType tokenType, String message, Throwable cause) {
        super(message, cause);
        this.tokenType = tokenType;
    }
}
