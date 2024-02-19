package com.toss.tosspaybackend.domain.bank.entity;

import com.toss.tosspaybackend.domain.bank.enums.TransactionType;
import com.toss.tosspaybackend.global.basetime.AuditListener;
import com.toss.tosspaybackend.global.basetime.Auditable;
import com.toss.tosspaybackend.global.basetime.BaseTime;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditListener.class)
@Entity
public class BankAccountTransactionHistory implements Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long amount; // 거래 금액
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // 거래 유형 (예: 입금, 출금)
    @ManyToOne
    @JoinColumn(name = "deposit_destination_id")
    private BankAccount depositDestination; // 입금처
    @ManyToOne
    @JoinColumn(name = "withdrawal_destination_id")
    private BankAccount withdrawalDestination; // 출금처
    private Long balanceAfterTransaction; // 거래 후 잔액

    @Setter
    @Embedded
    @Column(nullable = false)
    private BaseTime baseTime;

    @Builder
    public BankAccountTransactionHistory(Long amount, TransactionType transactionType, BankAccount depositDestination, BankAccount withdrawalDestination, Long balanceAfterTransaction) {
        this.amount = amount;
        this.transactionType = transactionType;
        this.depositDestination = depositDestination;
        this.withdrawalDestination = withdrawalDestination;
        this.balanceAfterTransaction = balanceAfterTransaction;
    }
}
