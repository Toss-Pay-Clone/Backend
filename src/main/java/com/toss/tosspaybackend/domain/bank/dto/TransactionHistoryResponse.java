package com.toss.tosspaybackend.domain.bank.dto;

import com.toss.tosspaybackend.domain.bank.entity.BankAccount;
import com.toss.tosspaybackend.domain.bank.entity.BankAccountTransactionHistory;
import com.toss.tosspaybackend.domain.bank.enums.TransactionType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TransactionHistoryResponse(
        Long id,
        Long amount,
        TransactionType transactionType,
        String description,
        Destination depositDestination,
        Destination withdrawaDestination,
        LocalDateTime transactionTime,
        Long balanceAfterTransaction
) {
    public static TransactionHistoryResponse fromEntityDeposit(BankAccountTransactionHistory entity) {
        return TransactionHistoryResponse.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .transactionType(entity.getTransactionType())
                .description(entity.getWithdrawalDestination().getMember().getName())
                .depositDestination(buildDestination(entity.getDepositDestination()))
                .withdrawaDestination(buildDestination(entity.getWithdrawalDestination()))
                .transactionTime(entity.getBaseTime().getCreatedAt())
                .balanceAfterTransaction(entity.getBalanceAfterTransaction())
                .build();
    }

    public static TransactionHistoryResponse fromEntityWithdrawal(BankAccountTransactionHistory entity) {
        return TransactionHistoryResponse.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .transactionType(entity.getTransactionType())
                .description(entity.getDepositDestination().getMember().getName())
                .depositDestination(buildDestination(entity.getDepositDestination()))
                .withdrawaDestination(buildDestination(entity.getWithdrawalDestination()))
                .transactionTime(entity.getBaseTime().getCreatedAt())
                .balanceAfterTransaction(entity.getBalanceAfterTransaction())
                .build();
    }

    private static Destination buildDestination(BankAccount bankAccount) {
        return Destination.builder()
                .bank(bankAccount.getBank())
                .name(bankAccount.getName())
                .bankAccountNumber(bankAccount.getBankAccountNumber())
                .build();
    }
}
