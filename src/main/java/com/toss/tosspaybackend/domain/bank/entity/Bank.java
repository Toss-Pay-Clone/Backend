package com.toss.tosspaybackend.domain.bank.entity;

import com.toss.tosspaybackend.domain.bank.BankName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bankId;

    @Column(unique = true)
    @Enumerated(EnumType.STRING)
    private BankName bankName;

    private Bank(BankName bankName) {
        this.bankName = bankName;
    }

    public static Bank of(BankName bankName) {
        return new Bank(bankName);
    }
}
