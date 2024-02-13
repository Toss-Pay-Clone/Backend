package com.toss.tosspaybackend.domain.bank.dto;

import com.toss.tosspaybackend.domain.bank.enums.Bank;
import jakarta.validation.Valid;

public record AddBankAccountRequest(
        @Valid Bank bank
) {
    public static AddBankAccountRequest of(Bank bank) {
        return new AddBankAccountRequest(bank);
    }
}
