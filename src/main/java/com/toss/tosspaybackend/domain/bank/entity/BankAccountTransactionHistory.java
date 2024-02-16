package com.toss.tosspaybackend.domain.bank.entity;

import com.toss.tosspaybackend.domain.bank.enums.TransactionType;
import com.toss.tosspaybackend.global.basetime.AuditListener;
import com.toss.tosspaybackend.global.basetime.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditListener.class)
@Entity
public class BankAccountTransactionHistory {
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
}
