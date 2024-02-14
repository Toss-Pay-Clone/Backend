package com.toss.tosspaybackend.domain.bank.service;

import com.toss.tosspaybackend.domain.bank.dto.AddBankAccountRequest;
import com.toss.tosspaybackend.domain.bank.dto.AddBankAccountResponse;
import com.toss.tosspaybackend.domain.bank.dto.GenerateAccountNumberResponse;
import com.toss.tosspaybackend.domain.bank.entity.BankAccount;
import com.toss.tosspaybackend.domain.bank.repository.BankAccountRepository;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.global.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class BankService {
    private final BankAccountRepository bankAccountRepository;

    public Response<AddBankAccountResponse> addBankAccount(AddBankAccountRequest request) {
        SecurityContext context = SecurityContextHolder.getContext();
        Long bankAccountNumber = createBankAccountNumber();
        Member member = (Member) context.getAuthentication().getPrincipal();
        BankAccount bankAccount = BankAccount.builder()
                .bankAccountNumber(bankAccountNumber)
                .name(request.bank().getKorName() + " 계좌")
                // 입금 기능 구현 후 초기 금액 입금 예정
                .balance(0L)
                .bank(request.bank())
                .member(member)
                // Password 설정은 회의 후 진행
                .password("1234")
                .build();
        BankAccount savedBankAccount = bankAccountRepository.save(bankAccount);
        return Response.<AddBankAccountResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("계좌가 성공적으로 연결되었습니다.")
                .data(AddBankAccountResponse.of(savedBankAccount))
                .build();
    }

    public Response<GenerateAccountNumberResponse> generateAccountNumber() {
        Long bankAccountNumber = createBankAccountNumber();

        return Response.<GenerateAccountNumberResponse>builder()
                .httpStatus(HttpStatus.CREATED)
                .message("랜덤 계좌번호를 생성했습니다.")
                .data(new GenerateAccountNumberResponse(bankAccountNumber))
                .build();
    }

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
