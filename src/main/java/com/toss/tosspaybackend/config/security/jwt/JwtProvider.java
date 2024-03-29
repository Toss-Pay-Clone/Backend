package com.toss.tosspaybackend.config.security.jwt;

import com.toss.tosspaybackend.config.security.SecurityProperties;
import com.toss.tosspaybackend.config.security.jwt.enums.TokenType;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import com.toss.tosspaybackend.global.exception.ErrorCode;
import com.toss.tosspaybackend.global.exception.GlobalException;
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
    private final MemberRepository memberRepository;

    public JwtToken createJWTTokens(Member loginMember) {
        Claims claims = getClaims(loginMember);

        String accessToken = getToken(loginMember, claims, securityProperties.getAccessTokenValidationMillisecond());
        String refreshToken = getToken(loginMember, claims, securityProperties.getRefreshTokenValidationMillisecond());

        return JwtToken.builder()
                .grantType("bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String refreshJWTToken(String refreshToken, Member loginMember, TokenType generateTokenType) {
        Claims claims = getTokenBodyClaims(refreshToken);

        if (generateTokenType.equals(TokenType.ACCESS_TOKEN)) {
            return getToken(loginMember, claims, securityProperties.getAccessTokenValidationMillisecond());
        } else if (generateTokenType.equals(TokenType.REFRESH_TOKEN)) {
            return getToken(loginMember, claims, securityProperties.getRefreshTokenValidationMillisecond());
        }
        return null;
    }

    private Claims getTokenBodyClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
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
                .setExpiration(new Date(now + (validationSecond * 1000L)))
                .compact();
    }
}
