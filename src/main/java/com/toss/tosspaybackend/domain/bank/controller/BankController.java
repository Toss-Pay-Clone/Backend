package com.toss.tosspaybackend.domain.bank.controller;

import com.toss.tosspaybackend.domain.bank.dto.AddBankAccountRequest;
import com.toss.tosspaybackend.domain.bank.dto.AddBankAccountResponse;
import com.toss.tosspaybackend.domain.bank.service.BankService;
import com.toss.tosspaybackend.global.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
