package com.toss.tosspaybackend.domain.bank.repository;

import com.toss.tosspaybackend.domain.bank.entity.BankAccountTransactionHistory;
import com.toss.tosspaybackend.domain.bank.enums.TransactionType;
import com.toss.tosspaybackend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountTransactionHistoryRepository extends JpaRepository<BankAccountTransactionHistory, Long> {
    List<BankAccountTransactionHistory> findByTransactionTypeAndDepositDestination_Member(TransactionType transactionType, Member member);
    List<BankAccountTransactionHistory> findByTransactionTypeAndWithdrawalDestination_Member(TransactionType transactionType, Member member);
}
