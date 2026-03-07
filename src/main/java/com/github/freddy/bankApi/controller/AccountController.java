package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.dto.UpdateStatusRequest;
import com.github.freddy.bankApi.dto.response.AccountResponse;
import com.github.freddy.bankApi.dto.response.ApiResponse;
import com.github.freddy.bankApi.dto.response.BalanceResponse;
import com.github.freddy.bankApi.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/{number}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @PathVariable String number,
            HttpServletRequest request
    ) {

        log.info("Staff consultando conta: {}", number);
        AccountResponse response = accountService.getAccount(number);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Conta recuperada com sucesso",
                        response,
                        null,
                        request.getRequestURI(),
                        OffsetDateTime.now()
                )
        );
    }


    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String userId = jwt.getSubject();
        log.debug("Cliente {} consultando saldo", userId);
        BalanceResponse balance = accountService.getBalance(userId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Consulta de saldo efectuada com sucesso",
                        balance,
                        null,
                        request.getRequestURI(),
                        OffsetDateTime.now()
                )
        );
    }

    // Atualizar status da conta (staff ou admin)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PatchMapping("/{number}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String number,
            @RequestBody @Valid UpdateStatusRequest request) {
        log.info("Atualizando status da conta {} para {}", number, request.status());
        accountService.changeStatusAccount(number, request.status());

        return ResponseEntity.noContent().build();
    }
}