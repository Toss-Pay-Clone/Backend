package com.toss.tosspaybackend.domain.bank.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Bank {
    KOOKMIN_BANK("KB국민은행"),
    KAKAO_BANK("카카오뱅크"),
    SHINHAN_BANK("신한은행"),
    NONGHYUP_BANK("NH농협은행"),
    HANA_BANK("하나은행"),
    WOORI_GLOBAL_BANKING("우리은행"),
    KOREA_POST_OFFICE("우체국");

    private String korName;
}
/*
    KB국민은행 (KOOKMIN BANK)
    카카오뱅크 (KakaoBank)
    신한은행 (Shinhan Bank)
    NH농협은행 (Nonghyup Bank)
    하나은행 (Hana Bank)
    우리은행 (Woori Global Banking)
    우체국 (KOREA POST OFFICE)
 */