package com.toss.tosspaybackend.config.security.jwt.enums;

public enum TokenStatus {
    VALID, EXPIRED, FORGED, INVALID, ACCESS_TOKEN_REGENERATION, REFRESH_TOKEN_REGENERATION
}
