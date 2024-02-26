package com.toss.tosspaybackend.domain.bank.service;

import com.toss.tosspaybackend.domain.bank.dto.*;
import com.toss.tosspaybackend.domain.bank.entity.BankAccount;
import com.toss.tosspaybackend.domain.bank.entity.BankAccountTransactionHistory;
import com.toss.tosspaybackend.domain.bank.enums.RemittanceStatus;
import com.toss.tosspaybackend.domain.bank.enums.TransactionType;
import com.toss.tosspaybackend.domain.bank.repository.BankAccountRepository;
import com.toss.tosspaybackend.domain.bank.repository.BankAccountTransactionHistoryRepository;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import com.toss.tosspaybackend.global.Response;
import com.toss.tosspaybackend.global.exception.ErrorCode;
import com.toss.tosspaybackend.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class BankService {
    private final BankAccountRepository bankAccountRepository;
    private final MemberRepository memberRepository;
    private final BankAccountTransactionHistoryRepository bankAccountTransactionHistoryRepository;

    public Response<AddBankAccountResponse> addBankAccount(AddBankAccountRequest request) {
        SecurityContext context = SecurityContextHolder.getContext();
        validateBankAccountNumber(request.bankAccountNumber());
        Member member = (Member) context.getAuthentication().getPrincipal();
        BankAccount bankAccount = BankAccount.builder()
                .bankAccountNumber(request.bankAccountNumber())
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

    private void validateBankAccountNumber(Long bankAccountNumber) {
        if (String.valueOf(bankAccountNumber).length() != 14) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "입력된 계좌번호 형식이 유효하지 않습니다.");
        }

        Optional<BankAccount> findBankAccount = bankAccountRepository.findById(bankAccountNumber);
        if (findBankAccount.isPresent()) {
            throw new GlobalException(ErrorCode.CONFLICT, "이미 존재하는 계좌번호입니다.");
        }
    }

    public Response<List<BankAccountListResponse>> getBankAccountList() {
        SecurityContext context = SecurityContextHolder.getContext();
        Member member = (Member) context.getAuthentication().getPrincipal();

        List<BankAccountListResponse> bankAccountDtoList = bankAccountRepository.findAllByMember(member).stream()
                .map(BankAccountListResponse::fromEntity)
                .toList();
        return Response.<List<BankAccountListResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("계좌 목록을 성공적으로 조회하였습니다.")
                .data(bankAccountDtoList)
                .build();
    }

    public Response<BankAccountListResponse> getBankAccount(Long id) {
        SecurityContext context = SecurityContextHolder.getContext();
        Member member = (Member) context.getAuthentication().getPrincipal();
        BankAccount findBankAccount = bankAccountRepository.findByMemberAndBankAccountNumber(member, id)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "계좌를 찾을 수 없습니다."));

        return Response.<BankAccountListResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("계좌를 성공적으로 조회하였습니다.")
                .data(BankAccountListResponse.fromEntity(findBankAccount))
                .build();
    }

    public Response<List<TransactionHistoryResponse>> getUserAccountTransactionList() {
        SecurityContext context = SecurityContextHolder.getContext();
        Member member = (Member) context.getAuthentication().getPrincipal();
        List<BankAccountTransactionHistory> depositHistoryList = bankAccountTransactionHistoryRepository.findByDepositDestination_MemberAndTransactionType(member, TransactionType.DEPOSIT);
        List<BankAccountTransactionHistory> withdrawaHistoryList = bankAccountTransactionHistoryRepository.findByWithdrawalDestination_MemberAndTransactionType(member, TransactionType.WITHDRAWAL);
        List<TransactionHistoryResponse> containsHistoryList = new ArrayList<>();
        containsHistoryList.addAll(depositHistoryList.stream().map(TransactionHistoryResponse::fromEntityDeposit).toList());
        containsHistoryList.addAll(withdrawaHistoryList.stream().map(TransactionHistoryResponse::fromEntityWithdrawal).toList());
        containsHistoryList.sort(Comparator.comparing(TransactionHistoryResponse::transactionTime));

        return Response.<List<TransactionHistoryResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("거래내역을 성공적으로 조회했습니다.")
                .data(containsHistoryList)
                .build();
    }

    public Response<TransactionHistoryResponse> getBankAccountTransaction(Long transactionNumber) {
        BankAccountTransactionHistory bankAccountTransactionHistory = bankAccountTransactionHistoryRepository.findById(transactionNumber)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "거래내역을 찾을 수 없습니다."));

        TransactionHistoryResponse responseData;
        if (bankAccountTransactionHistory.getTransactionType().equals(TransactionType.DEPOSIT)) {
            responseData = TransactionHistoryResponse.fromEntityDeposit(bankAccountTransactionHistory);
        } else {
            responseData = TransactionHistoryResponse.fromEntityWithdrawal(bankAccountTransactionHistory);
        }
        return Response.<TransactionHistoryResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("거래내역을 성공적으로 조회했습니다.")
                .data(responseData)
                .build();
    }

    public Response<List<TransactionHistoryResponse>> getAccountTransactionList(Long bankAccountNumber) {
        List<BankAccountTransactionHistory> depositHistoryList = bankAccountTransactionHistoryRepository.findByDepositDestination_BankAccountNumberAndTransactionType(bankAccountNumber, TransactionType.DEPOSIT);
        List<BankAccountTransactionHistory> withdrawaHistoryList = bankAccountTransactionHistoryRepository.findByWithdrawalDestination_BankAccountNumberAndTransactionType(bankAccountNumber, TransactionType.WITHDRAWAL);
        List<TransactionHistoryResponse> containsHistoryList = new ArrayList<>();
        containsHistoryList.addAll(depositHistoryList.stream().map(TransactionHistoryResponse::fromEntityDeposit).toList());
        containsHistoryList.addAll(withdrawaHistoryList.stream().map(TransactionHistoryResponse::fromEntityWithdrawal).toList());
        containsHistoryList.sort(Comparator.comparing(TransactionHistoryResponse::transactionTime));

        return Response.<List<TransactionHistoryResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("거래내역을 성공적으로 조회했습니다.")
                .data(containsHistoryList)
                .build();
    }

    @Transactional
    public Response<RemittanceStatus> sendRemittance(Long accountNumber, RemittanceRequest request) {
        BankAccount sourceAccount = bankAccountRepository.findById(accountNumber)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "계좌를 찾을 수 없습니다."));

        Optional<BankAccount> findTargetAccount = bankAccountRepository.findById(request.targetAccount());
        if (findTargetAccount.isEmpty()) {
            return Response.<RemittanceStatus>builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message("송금하려는 계좌가 존재하지 않음")
                    .data(RemittanceStatus.ACCOUNT_NOT_EXIST)
                    .build();
        }

        BankAccount targetAccount = findTargetAccount.get();

        if (sourceAccount.getBalance() < request.amount()) {
            return Response.<RemittanceStatus>builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .message("금액 부족")
                    .data(RemittanceStatus.INSUFFICIENT_FUNDS)
                    .build();
        }

        sourceAccount.decreaseBalance(request.amount());
        targetAccount.increaseBalance(request.amount());
        bankAccountRepository.save(sourceAccount);
        bankAccountRepository.save(targetAccount);

        bankAccountTransactionHistoryRepository.save(BankAccountTransactionHistory.builder()
                .amount(request.amount())
                .transactionType(TransactionType.WITHDRAWAL)
                .depositDestination(targetAccount)
                .withdrawalDestination(sourceAccount)
                .balanceAfterTransaction(sourceAccount.getBalance())
                .build());
        bankAccountTransactionHistoryRepository.save(BankAccountTransactionHistory.builder()
                .amount(request.amount())
                .transactionType(TransactionType.DEPOSIT)
                .depositDestination(targetAccount)
                .withdrawalDestination(sourceAccount)
                .balanceAfterTransaction(targetAccount.getBalance())
                .build());

        return Response.<RemittanceStatus>builder()
                .httpStatus(HttpStatus.OK)
                .message("송금 성공")
                .data(RemittanceStatus.SUCCESS)
                .build();
    }

    public Response<DateTransactionHistoryResponse> getDateTransactionList(DateTransactionRequest request) {
        SecurityContext context = SecurityContextHolder.getContext();
        Member member = (Member) context.getAuthentication().getPrincipal();
        List<BankAccountTransactionHistory> findTransactionHistoryList = bankAccountTransactionHistoryRepository.findByMemberAndBaseTime_CreatedAtYearAndMonth(member.getId(), request.year(), request.month());

        List<TransactionHistoryResponse> responseTransactionList = findTransactionHistoryList
                .stream()
                .map(history ->
                        history.getTransactionType().equals(TransactionType.DEPOSIT) ?
                                TransactionHistoryResponse.fromEntityDeposit(history) :
                                TransactionHistoryResponse.fromEntityWithdrawal(history))
                .sorted(Comparator.comparing(TransactionHistoryResponse::transactionTime).reversed())
                .toList();

        Long totalSpent = responseTransactionList
                .stream()
                .filter(rtl -> rtl.transactionType().equals(TransactionType.WITHDRAWAL))
                .mapToLong(TransactionHistoryResponse::amount)
                .sum();

        return Response.<DateTransactionHistoryResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("거래내역을 성공적으로 조회했습니다.")
                .data(DateTransactionHistoryResponse.builder()
                        .totalSpent(totalSpent)
                        .historyList(responseTransactionList)
                        .build())
                .build();
    }
}
