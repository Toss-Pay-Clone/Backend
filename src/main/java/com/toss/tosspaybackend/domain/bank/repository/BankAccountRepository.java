package com.toss.tosspaybackend.domain.bank.repository;

import com.toss.tosspaybackend.domain.bank.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
}
