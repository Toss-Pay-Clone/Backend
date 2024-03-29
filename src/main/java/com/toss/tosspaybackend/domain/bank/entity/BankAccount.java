package com.toss.tosspaybackend.domain.bank.entity;

import com.toss.tosspaybackend.domain.bank.enums.Bank;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.global.basetime.AuditListener;
import com.toss.tosspaybackend.global.basetime.Auditable;
import com.toss.tosspaybackend.global.basetime.BaseTime;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditListener.class)
@Entity
public class BankAccount implements Auditable {
    @Id
    private Long bankAccountNumber;

    private String name;
    private Long balance;

    @Enumerated(EnumType.STRING)
    private Bank bank;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    // 계좌 비밀번호
    private String password;

    @Setter
    @Embedded
    @Column(nullable = false)
    private BaseTime baseTime;

    @Builder
    public BankAccount(Long bankAccountNumber, String name, Long balance, Bank bank, Member member, String password) {
        this.bankAccountNumber = bankAccountNumber;
        this.name = name;
        this.balance = balance;
        this.bank = bank;
        this.member = member;
        this.password = password;
    }

    public void increaseBalance(Long amount) {
        this.balance += amount;
    }

    public void decreaseBalance(Long amount) {
        this.balance -= amount;
    }
}
