package com.toss.tosspaybackend.config.security.jwt;

import com.toss.tosspaybackend.config.security.SecurityProperties;
import com.toss.tosspaybackend.config.security.jwt.enums.TokenStatus;
import com.toss.tosspaybackend.config.security.jwt.enums.TokenType;
import com.toss.tosspaybackend.global.exception.CustomAccessDeniedHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
        // ! 이미 access_token을 가지고 login이나 register를 할 경우에도 JWT Filter를 거치는 문제 발견
        // Whitelist URI의 경우 JWT 필터를 거치지 않고 스킵
        if (isWhiteListed(requestULI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 이후 Request Debugging을 위해 남겨둠
        // System.out.println(requestULI);
        Optional<String> token = Optional.ofNullable(extractToken(request.getCookies()));



        token.ifPresentOrElse(
                // isPresent
                t -> {
                    try {
                        Authentication authentication = jwtValidator.getAuthentication(t);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        filterChain.doFilter(request, response);
                    } catch (AccessDeniedException ex) {
                        customAccessDeniedHandler.handle(request, response, ex);
                    } catch (ServletException | IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                // isEmpty
                () -> customAccessDeniedHandler.handle(request, response, new AccessDeniedException("Token is missing."))
            );
    }

    private String extractToken(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }

        Optional<Cookie> accessCookie = extractAccessToken(cookies);
        return accessCookie.map(Cookie::getValue).orElse(null);
    }

    /*
     * 헤더에 담긴 Access Token 추출
     */
    private Optional<Cookie> extractAccessToken(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(securityProperties.getAccessHeader()))
                .findFirst();
    }

    private boolean isWhiteListed(String requestURI) {
        for (String whiteListURL : securityProperties.getAuthWhitelist()) {
            if (requestURI.equals(whiteListURL) ||
                    (requestURI.charAt(requestURI.length() - 1) != '/' && requestURI.equals(whiteListURL + "/"))) {
                return true;
            }
        }
        return false;
    }
}
