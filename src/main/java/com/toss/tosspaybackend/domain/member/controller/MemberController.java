package com.toss.tosspaybackend.domain.member.controller;

import com.toss.tosspaybackend.config.security.jwt.JwtToken;
import com.toss.tosspaybackend.domain.member.dto.LoginRequest;
import com.toss.tosspaybackend.domain.member.dto.RegisterRequest;
import com.toss.tosspaybackend.domain.member.dto.RegisterResponse;
import com.toss.tosspaybackend.domain.member.service.MemberService;
import com.toss.tosspaybackend.global.Response;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public Response<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return memberService.register(request);
    }

    @PostMapping("/auth")
    public Response<JwtToken> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return memberService.login(request, response);
    }
}
