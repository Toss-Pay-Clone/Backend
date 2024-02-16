package com.toss.tosspaybackend.domain.bank.dto;

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
}
