package com.toss.tosspaybackend.config.security.jwt;

import com.toss.tosspaybackend.config.security.SecurityProperties;
import com.toss.tosspaybackend.config.security.jwt.enums.TokenStatus;
import com.toss.tosspaybackend.config.security.jwt.enums.TokenType;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.global.exception.CustomAccessDeniedHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtValidator jwtValidator;
    private final SecurityProperties securityProperties;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtValidator jwtValidator,
                                   SecurityProperties securityProperties,
                                   CustomAccessDeniedHandler customAccessDeniedHandler,
                                   JwtProvider jwtProvider) {
        this.jwtValidator = jwtValidator;
        this.securityProperties = securityProperties;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestULI = request.getRequestURI();
        if (isWhiteListed(requestULI)) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<String> accessToken = Optional.ofNullable(extractToken(request.getCookies(), TokenType.ACCESS_TOKEN));
        Optional<String> refreshToken = Optional.ofNullable(extractToken(request.getCookies(), TokenType.REFRESH_TOKEN));

        // 토큰이 두개 중 한개라도 존재하지 않을 경우 에러
        if (accessToken.isEmpty() || refreshToken.isEmpty()) {
            customAccessDeniedHandler.handle(request, response, new AccessDeniedException("Token is missing."));
        }

        try {
            TokenAuthentication tokenAuthentication = jwtValidator.getAuthentication(accessToken.get(), refreshToken.get());
//            log.info("tokenAuthentication -> tokenStatus: " + tokenAuthentication.tokenStatus());

            if (tokenAuthentication.tokenStatus().equals(TokenStatus.ACCESS_TOKEN_REGENERATION)) {
                // Access Token 재발급
                Member member = (Member) tokenAuthentication.authentication().getPrincipal();
                String refreshedToken = jwtProvider.refreshJWTToken(refreshToken.get(), member, TokenType.ACCESS_TOKEN);
                addCookie(response, refreshedToken, securityProperties.getAccessHeader(),
                        securityProperties.getAccessTokenValidationMillisecond());

            } else if (tokenAuthentication.tokenStatus().equals(TokenStatus.REFRESH_TOKEN_REGENERATION)) {
                // Refresh Token 재발급
                Member member = (Member) tokenAuthentication.authentication().getPrincipal();
                String refreshedToken = jwtProvider.refreshJWTToken(refreshToken.get(), member, TokenType.REFRESH_TOKEN);
                addCookie(response, refreshedToken, securityProperties.getRefreshHeader(),
                        securityProperties.getRefreshTokenValidationMillisecond());
            }

            SecurityContextHolder.getContext().setAuthentication(tokenAuthentication.authentication());
            filterChain.doFilter(request, response);
        } catch (AccessDeniedException ex) {
            customAccessDeniedHandler.handle(request, response, ex);
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void addCookie(HttpServletResponse response, String token, String header, int validationMillisecond) {
        Cookie tokenCookie = new Cookie(header, token);
        tokenCookie.setMaxAge(validationMillisecond);
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(true);
        response.addCookie(tokenCookie);
    }

    private String extractToken(Cookie[] cookies, TokenType tokenType) {
        if (cookies == null) {
            return null;
        }

        Optional<Cookie> accessCookie = switch (tokenType) {
            case ACCESS_TOKEN -> extractAccessToken(cookies);
            case REFRESH_TOKEN -> extractRefreshToken(cookies);
        };

        return accessCookie.map(Cookie::getValue).orElse(null);
    }

    // 헤더에 담긴 Access Token 추출
    private Optional<Cookie> extractAccessToken(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(securityProperties.getAccessHeader()))
                .findFirst();
    }

    // 헤더의 담긴 Refresh Token 추출
    private Optional<Cookie> extractRefreshToken(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(securityProperties.getRefreshHeader()))
                .findFirst();
    }

    private boolean isWhiteListed(String requestURI) {
        for (String whiteListURL : securityProperties.getAuthWhitelist()) {
            if (requestURI.startsWith(whiteListURL)) {
                return true;
            }
        }
        return false;
    }
}
