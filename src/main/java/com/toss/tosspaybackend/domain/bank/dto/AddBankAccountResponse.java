package com.toss.tosspaybackend.domain.bank.dto;

import com.toss.tosspaybackend.domain.bank.entity.BankAccount;
import com.toss.tosspaybackend.domain.bank.enums.Bank;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AddBankAccountResponse(
        Long bankAccountNumber,

        String name,
        Long balance,
        Bank bank,
        LocalDateTime createdAt
) {
    public static AddBankAccountResponse of(BankAccount bankAccount) {
        return AddBankAccountResponse.builder()
                .bankAccountNumber(bankAccount.getBankAccountNumber())
                .name(bankAccount.getName())
                .balance(bankAccount.getBalance())
                .bank(bankAccount.getBank())
                .createdAt(bankAccount.getBaseTime().getCreatedAt())
                .build();
    }
}
