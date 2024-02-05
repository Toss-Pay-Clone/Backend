package com.toss.tosspaybackend.domain.member.entity;

import com.toss.tosspaybackend.domain.member.enums.MobileCarrier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String phone;

    @Enumerated(EnumType.STRING)
    private String gender;
    @Enumerated(EnumType.STRING)
    private String nationality;

    private int firstResidentRegistrationNumber;
    private int secondResidentRegistrationNumber;

    @Enumerated(EnumType.STRING)
    private MobileCarrier mobileCarrier;
    private LocalDateTime birthdate;
}
