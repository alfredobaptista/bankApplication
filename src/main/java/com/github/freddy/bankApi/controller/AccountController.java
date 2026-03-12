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
    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountDetails(
            @PathVariable String accountNumber,
            HttpServletRequest request
    ) {
        log.info("Staff consultando conta: {}", accountNumber);
        AccountResponse response = accountService.getAccount(accountNumber);
        return okResponse("Conta recuperada com sucesso", response, request.getRequestURI());
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
        return okResponse("Consulta de saldo efectuada com sucesso", balance, request.getRequestURI());
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PatchMapping("/{number}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable String number,
            @RequestBody @Valid UpdateStatusRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Atualizando status da conta {} para {}", number, request.status());
        accountService.changeStatusAccount(number, request.status());
        return okResponse("Status da conta atualizado com sucesso", null, httpRequest.getRequestURI());
    }

    private <T> ResponseEntity<ApiResponse<T>> okResponse(String message, T data, String path) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        message,
                        data,
                        null,
                        path,
                        OffsetDateTime.now()
                )
        );
    }
}
