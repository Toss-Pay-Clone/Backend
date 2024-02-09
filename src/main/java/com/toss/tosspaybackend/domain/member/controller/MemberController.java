package com.toss.tosspaybackend.domain.member.controller;

import com.toss.tosspaybackend.config.security.jwt.JwtToken;
import com.toss.tosspaybackend.domain.member.dto.*;
import com.toss.tosspaybackend.domain.member.service.MemberService;
import com.toss.tosspaybackend.global.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public Response<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return memberService.register(request);
    }

    @PostMapping
    public Response<JwtToken> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return memberService.login(request, httpRequest, httpResponse);
    }

    @PostMapping("/existence-check")
    public Response<ExistenceCheckResponse> existenceCheck(@Valid @RequestBody ExistenceCheckRequest request, HttpServletResponse response) {
        return memberService.existenceCheck(request, response);
    }
}