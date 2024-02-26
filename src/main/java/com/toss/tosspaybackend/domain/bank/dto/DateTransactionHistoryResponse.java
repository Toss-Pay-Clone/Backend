package com.toss.tosspaybackend.domain.bank.dto;

import com.toss.tosspaybackend.domain.bank.entity.BankAccount;
import com.toss.tosspaybackend.domain.bank.entity.BankAccountTransactionHistory;
import com.toss.tosspaybackend.domain.bank.enums.TransactionType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DateTransactionHistoryResponse(
        Long totalSpent,
        List<TransactionHistoryResponse> historyList
) {
}
