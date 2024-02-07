package com.toss.tosspaybackend.domain.member.dto;

import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.enums.Gender;
import com.toss.tosspaybackend.domain.member.enums.MobileCarrier;
import com.toss.tosspaybackend.domain.member.enums.Nationality;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;


public record RegisterRequest(
        @NotNull String name,
        @NotNull String phone,
        @NotNull Gender gender,
        @NotNull String password,
        @NotNull Nationality nationality,
        @NotNull String residentRegistrationNumberFront,
        @NotNull String residentRegistrationNumberBack,
        @NotNull MobileCarrier mobileCarrier,
        @NotNull LocalDateTime birthdate
) {
    public Member toEntity(PasswordEncoder passwordEncoder) {
        return Member.of(name, phone, gender, password, nationality, residentRegistrationNumberFront,
                residentRegistrationNumberBack, mobileCarrier, birthdate, passwordEncoder);
    }
}