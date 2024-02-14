package com.toss.tosspaybackend.domain.bank.dto;

import com.toss.tosspaybackend.domain.bank.enums.Bank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AddBankAccountRequest(
        @NotNull Bank bank
) {
    public static AddBankAccountRequest of(Bank bank) {
        return new AddBankAccountRequest(bank);
    }
}
