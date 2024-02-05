package com.toss.tosspaybackend.domain.member.service;

import com.toss.tosspaybackend.domain.member.dto.RegisterRequest;
import com.toss.tosspaybackend.domain.member.dto.RegisterResponse;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.enums.Gender;
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
        validateRRN(request.residentRegistrationNumberFront(),
                request.residentRegistrationNumberBack(),
                request.gender());
        // TODO: 생일 검사 (주민번호 검사 포함)
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

    private void validatePhoneNumber(String phoneNumber) {
        // 전화번호에 010을 포함하고 있으며
        // 010을 제외한 자릿수가 7, 8자리 일 경우
        if (!phoneNumber.matches("^010\\d{7,8}$")) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "전화번호 형식이 유효하지 않습니다.");
        }
    }

    private void validateRRN(String frontRRN, String backRRN, Gender gender) {
        // 주민번호 앞자리 및 뒷자리 첫번째 검사
        if (!frontRRN.matches("^\\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\\d|3[0-1])$") ||
                !backRRN.matches("^[0-4|9]$")) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "주민번호 형식이 유효하지 않습니다.");
        }

        // 주민번호 - 성별 검사
        if ((gender == Gender.FEMALE && Integer.parseInt(backRRN) % 2 != 0) ||
                gender == Gender.MALE && Integer.parseInt(backRRN) % 2 != 1) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "주민번호와 성별이 일치하지 않습니다.");
        }
    }


}
