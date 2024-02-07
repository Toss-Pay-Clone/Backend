package com.toss.tosspaybackend.domain.member.service;

import com.toss.tosspaybackend.domain.member.dto.LoginRequest;
import com.toss.tosspaybackend.domain.member.dto.LoginResponse;
import com.toss.tosspaybackend.domain.member.dto.RegisterRequest;
import com.toss.tosspaybackend.domain.member.dto.RegisterResponse;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import com.toss.tosspaybackend.domain.member.service.validate.MemberValidate;
import com.toss.tosspaybackend.global.Response;
import com.toss.tosspaybackend.global.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.toss.tosspaybackend.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberValidate memberValidate;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Response<RegisterResponse> register(RegisterRequest request) {

        memberValidate.validatePhoneNumber(request.phone());
        memberValidate.validateRRN(request.residentRegistrationNumberFront(), request.residentRegistrationNumberBack());
        memberValidate.validateGender(request.gender(), request.residentRegistrationNumberBack());
        memberValidate.validateBirthdate(request.birthdate(), request.residentRegistrationNumberFront(),
                request.residentRegistrationNumberBack(), request.gender());
        memberValidate.validateDuplicate(request.name(), request.phone(), request.residentRegistrationNumberFront());
        memberValidate.validatePassword(request.password(), request.phone(), request.birthdate());

        Member savedMember = memberRepository.save(request.toEntity(passwordEncoder));

        RegisterResponse responseData = RegisterResponse.builder()
                .id(savedMember.getId())
                .name(savedMember.getName())
                .createdAt(savedMember.getBaseTime().getCreatedAt())
                .build();

        return Response.<RegisterResponse>builder()
                .httpStatus(HttpStatus.CREATED.value())
                .message("회원가입에 성공했습니다.")
                .data(responseData)
                .build();
    }

    @Transactional(readOnly = true)
        Member member = memberRepository.findByPhone(request.phone())
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "해당 전화번호로 가입된 계정이 없습니다."));

        // 비밀번호가 일치하는가?
        memberValidate.checkPassword(member, request.password(), passwordEncoder);

        LoginResponse responseData = LoginResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .phone(member.getPhone())
                .build();

        return Response.<LoginResponse>builder()
                .httpStatus(HttpStatus.OK.value())
                .message("로그인에 성공했습니다.")
                .data(responseData)
                .build();
    }
}
