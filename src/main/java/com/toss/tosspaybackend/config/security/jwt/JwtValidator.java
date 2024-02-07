package com.toss.tosspaybackend.config.security.jwt;

import com.toss.tosspaybackend.config.security.jwt.enums.TokenStatus;
import com.toss.tosspaybackend.config.security.jwt.enums.TokenType;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtValidator {
    private final Key key;
    private final MemberRepository memberRepository;

    public Authentication getAuthentication(String accessToken, String refreshToken) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Refresh Token Validation
        try {
            Claims claims = getTokenBodyClaims(refreshToken);
            // ? 이건 남겨야하나? 검사하면 좋을꺼같긴 한데 쿼리문을 한번 더 쓰는 오버헤드가 있네
            // TODO: 만약 쓸꺼면 exists 쿼리로 사용
            // TODO: 만료 시간 검사 후 얼마 안남았으면 재발급
            Member member = memberRepository.findById(claims.get("id", Long.class))
                    .orElseThrow(() -> new AccessDeniedException("Invalid Token"));
        } catch (JwtException e) {
            // TODO: 재발급 로직이 들어갈 수 도 있음
            getTokenStatus(e, TokenType.REFRESH_TOKEN);
        } catch (Exception e) {
            log.error("JWT Exception", e);
        }

        // Access Token Validation
        try {
            Claims claims = getTokenBodyClaims(accessToken);
            Member member = memberRepository.findById(claims.get("id", Long.class))
                    .orElseThrow(() -> new AccessDeniedException("Invalid Token"));

            return new UsernamePasswordAuthenticationToken(member, "", authorities);

        } catch (JwtException e) {
            TokenStatus tokenStatus = getTokenStatus(e, TokenType.ACCESS_TOKEN);

            if (tokenStatus == TokenStatus.ACCESS_TOKEN_REGENERATION) {
                // TODO: 재발급 로직 구현
            }
            
            handleTokenStatus(tokenStatus);
        } catch (Exception e) {
            log.error("JWT Exception", e);
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

    private TokenStatus getTokenStatus(JwtException e, TokenType tokenType) {
        if (tokenType == TokenType.ACCESS_TOKEN && e instanceof ExpiredJwtException) {
            return TokenStatus.ACCESS_TOKEN_REGENERATION;
        }

        if (e instanceof ExpiredJwtException) {
            return TokenStatus.EXPIRED;
        } else if (e instanceof MalformedJwtException || e instanceof SignatureException) {
            // TODO: 위변조 Check 후 차단 로직 구현 and Log 생성
            return TokenStatus.FORGED;
        } else {
            // TODO: Token이 잘못 생성된 경우나 위변조의 경우가 있어 Log생성
            return TokenStatus.INVALID;
        }
    }

    private void handleTokenStatus(TokenStatus status) {
        switch (status) {
            case EXPIRED -> throw new AccessDeniedException("Expired Token");
            case FORGED -> throw new AccessDeniedException("Forged Token");
            case INVALID -> throw new AccessDeniedException("Invalid Token");
        }
    }
}
