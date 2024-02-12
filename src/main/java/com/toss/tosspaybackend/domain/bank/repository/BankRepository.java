package com.toss.tosspaybackend.domain.bank.repository;

import com.toss.tosspaybackend.domain.bank.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, Long> {
}
