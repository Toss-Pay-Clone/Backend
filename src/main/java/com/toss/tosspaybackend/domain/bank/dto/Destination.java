package com.toss.tosspaybackend.domain.bank.dto;

import com.toss.tosspaybackend.domain.bank.enums.Bank;
import lombok.Builder;

@Builder
public record Destination(
        Bank bank,
        String name,
        Long bankAccountNumber
) {
}
