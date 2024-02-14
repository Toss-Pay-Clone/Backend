package com.toss.tosspaybackend.domain.bank.dto;

import com.toss.tosspaybackend.domain.bank.entity.BankAccount;
import com.toss.tosspaybackend.domain.bank.enums.Bank;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BankAccountListResponse {
    private Long bankAccountNumber;
    private String name;
    private Long balance;
    private Bank bank;

    public static BankAccountListResponse fromEntity(BankAccount bankAccount) {
        return BankAccountListResponse.builder()
                .bankAccountNumber(bankAccount.getBankAccountNumber())
                .name(bankAccount.getName())
                .balance(bankAccount.getBalance())
                .bank(bankAccount.getBank())
                .build();
    }
}