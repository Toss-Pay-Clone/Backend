package com.toss.tosspaybackend.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.FORBIDDEN_ACCESS, accessDeniedException.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        try {
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            log.error("CustomAccessDeniedHandler: IOException occurred", e);
        }
    }
}
