package com.toss.tosspaybackend.domain.bank.dto;

import com.toss.tosspaybackend.domain.bank.enums.Bank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AddBankAccountRequest(
        @NotNull Bank bank,
        @NotNull Long bankAccountNumber
) {
    public static AddBankAccountRequest of(Bank bank, Long bankAccountNumber) {
        return new AddBankAccountRequest(bank, bankAccountNumber);
    }
}
