package com.toss.tosspaybackend.domain.bank.service;

import com.toss.tosspaybackend.domain.bank.dto.AddBankAccountRequest;
import com.toss.tosspaybackend.domain.bank.dto.AddBankAccountResponse;
import com.toss.tosspaybackend.domain.bank.entity.BankAccount;
import com.toss.tosspaybackend.domain.bank.repository.BankAccountRepository;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.global.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class BankService {
    private final BankAccountRepository bankAccountRepository;

    private Long createBankAccountNumber() {
        Optional<BankAccount> findBankAccount = Optional.empty();
        Long randomAccountNumber = 0L;
        do {
            Random random = new Random();

            StringBuilder accountNumber = new StringBuilder(String.valueOf(random.nextInt(9) + 1)); // 첫 자리는 1~9 사이의 랜덤 숫자

            for (int i = 1; i < 14; i++) { // 나머지 13자리는 0~9 사이의 랜덤 숫자
                accountNumber.append(random.nextInt(10));
            }
            randomAccountNumber = Long.parseLong(accountNumber.toString());

            findBankAccount = bankAccountRepository.findById(randomAccountNumber);
        } while (findBankAccount.isPresent());

        return randomAccountNumber;
    }
}
