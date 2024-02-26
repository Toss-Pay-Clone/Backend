package com.toss.tosspaybackend.domain.bank.repository;

import com.toss.tosspaybackend.domain.bank.entity.BankAccountTransactionHistory;
import com.toss.tosspaybackend.domain.bank.enums.TransactionType;
import com.toss.tosspaybackend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BankAccountTransactionHistoryRepository extends JpaRepository<BankAccountTransactionHistory, Long> {
    List<BankAccountTransactionHistory> findByDepositDestination_MemberAndTransactionType(Member member, TransactionType transactionType);
    List<BankAccountTransactionHistory> findByWithdrawalDestination_MemberAndTransactionType(Member member, TransactionType transactionType);
    List<BankAccountTransactionHistory> findByDepositDestination_BankAccountNumberAndTransactionType(Long bankAccountNumber, TransactionType transactionType);
    List<BankAccountTransactionHistory> findByWithdrawalDestination_BankAccountNumberAndTransactionType(Long bankAccountNumber, TransactionType transactionType);
    @Query("SELECT b FROM BankAccountTransactionHistory b WHERE (b.depositDestination.member.id = :memberId OR b.withdrawalDestination.member.id = :memberId) AND YEAR(b.baseTime.createdAt) = :year AND MONTH(b.baseTime.createdAt) = :month")
    List<BankAccountTransactionHistory> findByMemberAndBaseTime_CreatedAtYearAndMonth(@Param("memberId") Long memberId, @Param("year") int year, @Param("month") int month);
}
