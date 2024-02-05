package com.toss.tosspaybackend.domain.member.entity;

import com.toss.tosspaybackend.domain.member.enums.Gender;
import com.toss.tosspaybackend.domain.member.enums.MobileCarrier;
import com.toss.tosspaybackend.domain.member.enums.Nationality;
import com.toss.tosspaybackend.global.basetime.Auditable;
import com.toss.tosspaybackend.global.basetime.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member implements Auditable {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    private Nationality nationality;

    private int firstResidentRegistrationNumber;
    private int secondResidentRegistrationNumber;

    @Enumerated(EnumType.STRING)
    private MobileCarrier mobileCarrier;
    private LocalDateTime birthdate;

    @Setter
    @Embedded
    @Column(nullable = false)
    private BaseTime baseTime;
}
