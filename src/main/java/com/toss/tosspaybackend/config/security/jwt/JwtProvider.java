package com.toss.tosspaybackend.config.security.jwt;

import com.toss.tosspaybackend.config.security.SecurityProperties;
import com.toss.tosspaybackend.domain.member.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final Key key;
    private final SecurityProperties securityProperties;

    public JwtToken createJWTTokens(Member loginMember) {
        Claims claims = getClaims(loginMember);

        String accessToken = getToken(loginMember, claims, securityProperties.getAccessTokenValidationSecond());
        String refreshToken = getToken(loginMember, claims, securityProperties.getRefreshTokenValidationSecond());

        return JwtToken.builder()
                .grantType("bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private Claims getClaims(Member loginMember) {
        Claims claims = Jwts.claims();
        claims.put("id", loginMember.getId());

        return claims;
    }

    private String getToken(Member loginMember, Claims claims, int validationSecond) {
        final long now = new Date().getTime();

        return Jwts.builder()
                .setSubject(loginMember.getName())
                .setClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(new Date(now + validationSecond))
                .compact();
    }
}
