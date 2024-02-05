package com.toss.tosspaybackend.domain.member.service;

import com.toss.tosspaybackend.domain.member.dto.RegisterRequest;
import com.toss.tosspaybackend.domain.member.dto.RegisterResponse;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import com.toss.tosspaybackend.global.Response;
import com.toss.tosspaybackend.global.exception.ErrorCode;
import com.toss.tosspaybackend.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public Response<RegisterResponse> register(RegisterRequest request) {

        validatePhoneNumber(request.phone());
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

    public void validatePhoneNumber(String phoneNumber) {
        // 전화번호에 010을 포함하고 있으며
        // 010을 제외한 자릿수가 7, 8자리 일 경우
        if (!phoneNumber.matches("^010\\d{7,8}$")) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "전화번호 형식이 유효하지 않습니다.");
        }
    }
}
