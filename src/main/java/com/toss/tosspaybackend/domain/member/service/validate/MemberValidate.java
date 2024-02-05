package com.toss.tosspaybackend.domain.member.service.validate;

import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.enums.Gender;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import com.toss.tosspaybackend.global.exception.ErrorCode;
import com.toss.tosspaybackend.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class MemberValidate {

    private final MemberRepository memberRepository;

    public void validatePhoneNumber(String phoneNumber) {
        // 전화번호에 010을 포함하고 있으며
        // 010을 제외한 자릿수가 7, 8자리 일 경우
        if (!phoneNumber.matches("^010\\d{7,8}$")) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "전화번호 형식이 유효하지 않습니다.");
        }
    }

    public void validateRRN(String frontRRN, String backRRN) {
        // 주민번호 앞자리 및 뒷자리 첫번째 검사
        if (!frontRRN.matches("^\\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\\d|3[0-1])$") ||
                !backRRN.matches("^[0-4|9]$")) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "주민번호 형식이 유효하지 않습니다.");
        }
    }

    public void validateGender(Gender gender, String backRRN) {
        // 주민번호 - 성별 검사
        if ((gender == Gender.FEMALE && Integer.parseInt(backRRN) % 2 != 0) ||
                gender == Gender.MALE && Integer.parseInt(backRRN) % 2 != 1) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "주민번호와 성별이 일치하지 않습니다.");
        }
    }

    public void validateBirthdate(LocalDateTime birthdate, String frontRRN, String backRRN, Gender gender) {
        // 생일과 주민번호 앞자리가 일치하는가?
        int birthdateYearFront = birthdate.getYear() / 100;
        int birthdateYearBack = birthdate.getYear() % 100;
        int birthdateMonth = birthdate.getMonthValue();
        int birthdateDay = birthdate.getDayOfMonth();

        int rrnYear = Integer.parseInt(frontRRN.substring(0, 2));
        int rrnMonth = Integer.parseInt(frontRRN.substring(2, 4));
        int rrnDay = Integer.parseInt(frontRRN.substring(4, 6));

        if (birthdateYearBack != rrnYear &&
                birthdateMonth != rrnMonth &&
                birthdateDay != rrnDay) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "주민번호와 생일이 일치하지 않습니다.");
        }

        // 생일과 주민번호 뒷자리가 일치하는가?
        Map<Integer, String> yearToRRN = new HashMap<>();
        yearToRRN.put(18, gender == Gender.MALE ? "9" : "0");
        yearToRRN.put(19, gender == Gender.MALE ? "1" : "2");
        yearToRRN.put(20, gender == Gender.MALE ? "3" : "4");

        if (!yearToRRN.get(birthdateYearFront).equals(backRRN)) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "주민번호와 성별이 일치하지 않습니다.");
        }
    }
    public void validateDuplicate(String name, String phone, String frontRRN) {
        Optional<Member> findMember = memberRepository.findByNameAndPhoneAndResidentRegistrationNumberFront(name, phone, frontRRN);
        if (findMember.isEmpty()) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "중복된 계정이 있습니다.");
        }
    }
}
