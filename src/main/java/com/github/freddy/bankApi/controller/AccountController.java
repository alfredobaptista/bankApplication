package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.docs.AccountControllerDocs;
import com.github.freddy.bankApi.dto.UpdateStatusRequest;
import com.github.freddy.bankApi.dto.response.AccountResponse;
import com.github.freddy.bankApi.dto.response.ApiResponseDTO;
import com.github.freddy.bankApi.dto.response.BalanceResponse;
import com.github.freddy.bankApi.service.AccountService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController implements AccountControllerDocs {

    private final AccountService accountService;
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponseDTO<AccountResponse>> getAccountDetails(
            @Parameter(description = "Número da conta a ser consultada") @PathVariable String accountNumber,
            HttpServletRequest request
    ) {
        log.info("Staff consultando conta: {}", accountNumber);
        AccountResponse response = accountService.getAccount(accountNumber);
        return okResponse("Conta recuperada com sucesso", response, request.getRequestURI());
    }


    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/balance")
    public ResponseEntity<ApiResponseDTO<BalanceResponse>> getBalance(
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
    public ResponseEntity<ApiResponseDTO<Void>> updateStatus(
            @Parameter(description = "Número da conta a ser atualizada") @PathVariable String number,
            @Parameter(description = "Novo status da conta") @RequestBody @Valid UpdateStatusRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Atualizando status da conta {} para {}", number, request.status());
        accountService.changeStatusAccount(number, request.status());
        return okResponse("Status da conta atualizado com sucesso", null, httpRequest.getRequestURI());
    }

    private <T> ResponseEntity<ApiResponseDTO<T>> okResponse(String message, T data, String path) {
        return ResponseEntity.ok(
                new ApiResponseDTO<>(
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
