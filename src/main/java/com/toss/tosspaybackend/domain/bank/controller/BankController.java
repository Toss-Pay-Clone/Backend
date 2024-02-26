package com.toss.tosspaybackend.domain.bank.controller;

import com.toss.tosspaybackend.domain.bank.dto.*;
import com.toss.tosspaybackend.domain.bank.enums.RemittanceStatus;
import com.toss.tosspaybackend.domain.bank.service.BankService;
import com.toss.tosspaybackend.global.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/bank")
@RestController
public class BankController {
    private final BankService bankService;

    @PreAuthorize("hasAuthority(@roleService.getRoleUser())")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/account")
    public Response<AddBankAccountResponse> addBankAccount(@Valid @RequestBody AddBankAccountRequest request) {
        return bankService.addBankAccount(request);
    }

    @PreAuthorize("hasAuthority(@roleService.getRoleUser())")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/account/gen-number")
    public Response<GenerateAccountNumberResponse> generateAccountNumber() {
        return bankService.generateAccountNumber();
    }

    @PreAuthorize("hasAuthority(@roleService.getRoleUser())")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/account")
    public Response<List<BankAccountListResponse>> bankAccountList() {
        return bankService.getBankAccountList();
    }

    @PreAuthorize("hasAuthority(@roleService.getRoleUser())")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/account/{accountNumber}")
    public Response<BankAccountListResponse> bankAccount(@PathVariable("accountNumber") Long accountNumber) {
        return bankService.getBankAccount(accountNumber);
    }

    @PreAuthorize("hasAuthority(@roleService.getRoleUser())")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/accounts/transactions")
    public Response<List<TransactionHistoryResponse>> accountTransactionList() {
        return bankService.getUserAccountTransactionList();
    }

    @PreAuthorize("hasAuthority(@roleService.getRoleUser())")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/accounts/transactions/{transactionNumber}")
    public Response<TransactionHistoryResponse> bankAccountTransaction(@PathVariable("transactionNumber") Long transactionNumber) {
        return bankService.getBankAccountTransaction(transactionNumber);
    }

    @PreAuthorize("hasAuthority(@roleService.getRoleUser())")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/account/{accountNumber}/transactions")
    public Response<List<TransactionHistoryResponse>> bankAccountTransactionList(@PathVariable("accountNumber") Long accountNumber) {
        return bankService.getAccountTransactionList(accountNumber);
    }

    @PreAuthorize("hasAuthority(@roleService.getRoleUser())")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/account/{accountNumber}/remittance")
    public Response<RemittanceStatus> sendRemittance(@PathVariable("accountNumber") Long accountNumber,
                                                     @RequestBody RemittanceRequest request) {
        return bankService.sendRemittance(accountNumber, request);
    }

    @PreAuthorize("hasAuthority(@roleService.getRoleUser())")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/accounts/transactions/date")
    public Response<DateTransactionHistoryResponse> accountDateTransactionList(@RequestBody DateTransactionRequest request) {
        return bankService.getDateTransactionList(request);
    }
}
