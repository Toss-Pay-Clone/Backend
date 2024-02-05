package com.toss.tosspaybackend.domain.member.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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

    private String gender;
    private String nationality;

    private int firstResidentRegistrationNumber;
    private int secondResidentRegistrationNumber;

    private String mobileCarrier;
    private LocalDateTime birthdate;
}
