package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.dto.UpdateStatusRequest;
import com.github.freddy.bankApi.dto.response.AccountResponse;
import com.github.freddy.bankApi.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{number}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String number) {
        return ResponseEntity.ok(accountService.getAccount(number));
    }

    @GetMapping("/{number}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String number) {
        return ResponseEntity.ok(accountService.getBalance(number));
    }

    @PatchMapping("/{number}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<Void> updateStatus(@PathVariable String number,
                                             @RequestBody @Valid UpdateStatusRequest request) {
        accountService.changeStatusAccount(number, request.status());
        return ResponseEntity.noContent().build();
    }

}