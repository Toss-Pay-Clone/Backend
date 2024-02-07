package com.toss.tosspaybackend.domain.member.service.validate;

import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.enums.Gender;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import com.toss.tosspaybackend.global.exception.ErrorCode;
import com.toss.tosspaybackend.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        if (!phoneNumber.matches("^010\\d{8}$")) {
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

        if (birthdateYearBack != rrnYear ||
                birthdateMonth != rrnMonth ||
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
        Optional<Member> findPhoneMember = memberRepository.findByPhone(phone);
        Optional<Member> findDeletedPhoneMember = memberRepository.findByPhoneAndDeleted(phone);

        if (findDeletedPhoneMember.isPresent()) {
            throw new GlobalException(ErrorCode.CONFLICT, "이미 탈퇴한 사용자입니다. 다른 정보로 가입해주세요.");
        }

        if (findPhoneMember.isPresent()) {
            throw new GlobalException(ErrorCode.CONFLICT, "이미 사용중인 전화번호입니다.");
        }

        Optional<Member> findDuplicatedMember = memberRepository.findByNameAndPhoneAndResidentRegistrationNumberFront(name, phone, frontRRN);
        if (findDuplicatedMember.isPresent()) {
            throw new GlobalException(ErrorCode.CONFLICT, "중복된 계정이 있습니다.");
        }
    }

    public void validatePassword(String password, String phoneNumber, LocalDateTime birthdate) {
        if (!password.matches("^\\d{4}[a-zA-Z]$")) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "비밀번호 형식이 유효하지 않습니다.");
        }

        String digits = password.substring(0, 4);

        // 숫자 부분이 전화번호나 생년월일에 포함되지 않는지 확인
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String birthdateStr = birthdate.format(formatter);

        if (phoneNumber.contains(digits) || birthdateStr.contains(digits)) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "비밀번호에 전화번호나 생년월일이 포함될 수 없습니다.");
        }

        // 숫자 부분에 연속 또는 중복된 숫자가 있는지 확인
        for (int i = 0; i < digits.length() - 1; i++) {
            if (digits.charAt(i) == digits.charAt(i + 1) ||
                    Math.abs(digits.charAt(i) - digits.charAt(i + 1)) == 1) {
                throw new GlobalException(ErrorCode.BAD_REQUEST, "비밀번호에 연속되거나 중복되는 숫자를 사용할 수 없습니다.");
            }
        }
    }

    public void checkPassword(Member member, String password, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "비밀번호가 일치하지 않습니다.");
        }
    }
}
