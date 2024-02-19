package com.toss.tosspaybackend.domain.bank.dto;

public record RemittanceRequest(
        Long targetAccount,
        Long amount
) {
}
