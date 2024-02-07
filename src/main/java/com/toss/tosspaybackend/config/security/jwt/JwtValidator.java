package com.toss.tosspaybackend.config.security.jwt;

import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import com.toss.tosspaybackend.global.exception.ErrorCode;
import com.toss.tosspaybackend.global.exception.CustomAccessDeniedHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class JwtValidator {
    private final Key key;
    private final MemberRepository memberRepository;

    public Authentication getAuthentication(String accessToken) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        try {
            Claims claims = getTokenBodyClaims(accessToken);
            Member member = memberRepository.findById(claims.get("id", Long.class))
                    .orElseThrow(() -> new AccessDeniedException("Invalid Token"));

            return new UsernamePasswordAuthenticationToken(member, "", authorities);
        } catch (ExpiredJwtException e) {
            throw new AccessDeniedException("Token expired");
        }


    }

    private Claims getTokenBodyClaims(String accessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
    }
}
