package com.toss.tosspaybackend.domain.member.dto;

import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.enums.Gender;
import com.toss.tosspaybackend.domain.member.enums.MobileCarrier;
import com.toss.tosspaybackend.domain.member.enums.Nationality;

import java.time.LocalDateTime;

public record RegisterRequest(
        String name,
        String phone,
        Gender gender,
        Nationality nationality,
        String residentRegistrationNumberFront,
        String residentRegistrationNumberBack,
        MobileCarrier mobileCarrier,
        LocalDateTime birthdate
) {
    public Member toEntity() {
        return Member.of(name, phone, gender, nationality, residentRegistrationNumberFront,
                residentRegistrationNumberBack, mobileCarrier, birthdate);
    }
}