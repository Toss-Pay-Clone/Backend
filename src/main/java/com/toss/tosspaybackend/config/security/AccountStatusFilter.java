package com.toss.tosspaybackend.config.security;

import com.toss.tosspaybackend.config.security.jwt.JwtProvider;
import com.toss.tosspaybackend.config.security.jwt.JwtValidator;
import com.toss.tosspaybackend.config.security.jwt.TokenAuthentication;
import com.toss.tosspaybackend.config.security.jwt.enums.TokenStatus;
import com.toss.tosspaybackend.config.security.jwt.enums.TokenType;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.enums.AccountStatus;
import com.toss.tosspaybackend.global.exception.CustomAccessDeniedHandler;
import com.toss.tosspaybackend.global.exception.ErrorCode;
import com.toss.tosspaybackend.global.exception.GlobalException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountStatusFilter extends OncePerRequestFilter {
    private final SecurityProperties securityProperties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestULI = request.getRequestURI();
        if (isWhiteListed(requestULI)) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityContext context = SecurityContextHolder.getContext();
        Member member = (Member) context.getAuthentication().getPrincipal();
        accountStatusValidate(member);

        filterChain.doFilter(request, response);
    }

    private boolean isWhiteListed(String requestURI) {
        for (String whiteListURL : securityProperties.getAuthWhitelist()) {
            if (requestURI.startsWith(whiteListURL)) {
                return true;
            }
        }
        return false;
    }

    private void accountStatusValidate(Member member) {
        AccountStatus accountStatus = member.getAccountStatus();

        if (accountStatus.equals(AccountStatus.BANNED)) {
            throw new AccessDeniedException("계정이 정지되었습니다.");
        } else if (accountStatus.equals(AccountStatus.SUSPENDED)) {
            throw new AccessDeniedException("계정이 일시 정지되었습니다.");
        }
    }
}
