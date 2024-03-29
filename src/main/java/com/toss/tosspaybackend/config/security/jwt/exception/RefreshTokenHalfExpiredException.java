package com.toss.tosspaybackend.config.security.jwt.exception;

import com.toss.tosspaybackend.config.security.jwt.enums.TokenType;
import com.toss.tosspaybackend.config.security.jwt.exception.CustomJwtException;
import lombok.Getter;

@Getter
public class RefreshTokenHalfExpiredException extends CustomJwtException {
    private final Long memberId;

    public RefreshTokenHalfExpiredException(Long memberId) {
        super(TokenType.REFRESH_TOKEN, TokenType.REFRESH_TOKEN.name());
        this.memberId = memberId;
    }
}
