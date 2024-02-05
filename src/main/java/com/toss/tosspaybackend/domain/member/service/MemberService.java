package com.toss.tosspaybackend.domain.member.service;

import com.toss.tosspaybackend.domain.member.dto.RegisterRequest;
import com.toss.tosspaybackend.domain.member.dto.RegisterResponse;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import com.toss.tosspaybackend.global.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public Response<RegisterResponse> register(RegisterRequest request) {
        // TODO: 전화번호 검사
        // TODO: 주민번호 검사
        // TODO: 생일 검사
        // TODO: 중복 검사

        Member savedMember = memberRepository.save(request.toEntity());

        RegisterResponse responseData = RegisterResponse.builder()
                .id(savedMember.getId())
                .name(savedMember.getName())
                .createdAt(savedMember.getBaseTime().getCreatedAt())
                .build();

        return Response.<RegisterResponse>builder()
                .httpStatus(HttpStatus.CREATED)
                .message("회원가입에 성공했습니다.")
                .data(responseData)
                .build();
    }
}
