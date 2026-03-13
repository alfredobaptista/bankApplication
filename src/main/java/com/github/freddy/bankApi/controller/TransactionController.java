package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.docs.TransactionControllerDocs;
import com.github.freddy.bankApi.dto.request.CardlessWithdrawRequest;
import com.github.freddy.bankApi.dto.request.DepositRequest;
import com.github.freddy.bankApi.dto.response.*;
import com.github.freddy.bankApi.dto.request.TransferRequest;
import com.github.freddy.bankApi.service.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Controller para operações de transações bancárias.
 * Todos os endpoints exigem autenticação JWT e verificam propriedade da conta.
 */

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController implements TransactionControllerDocs {
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;
    /**
     * Realiza uma transferência entre contas.
     * - O usuário logado deve ser o dono da conta de origem
     * - Retorna o novo saldo da conta de origem
     */
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponseDTO<TransferResponse>> transfer(
            @Valid @RequestBody TransferRequest dto,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String userId = jwt.getSubject();
        log.debug("Transferência solicitada por usuário ID: {} - Para: {}  Valor: {}",
                userId, dto.accountNumber(), dto.amount());

        TransferResponse response = transactionService.transfer(dto, userId);
        log.info("Transferência realizada com sucesso - Usuário ID: {}, Para: {}, Valor: {}",
                userId,  dto.accountNumber(), dto.amount());

        return ResponseEntity.ok().body(
                new ApiResponseDTO<TransferResponse>(
                true,
                "Transfrencia Realizda com sucesso!",
                response,
                null,
                request.getRequestURI(),
                OffsetDateTime.now()
        ));
    }

    /**
     * Realiza um depósito na conta especificada.
     * - O usuário logado deve ser o dono da conta
     * - Retorna o novo saldo da conta
     */

    @PreAuthorize("hasRole('ROLE_STAFF')")
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponseDTO<DepositResponse>> deposit(
            @Valid @RequestBody DepositRequest dto,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String userId = jwt.getSubject();
        log.debug("Depósito solicitado por usuário ID: {} - Conta: {} Valor: {}",
                userId, dto.accountNumber(), dto.amount());

        DepositResponse newDeposit = transactionService.deposit(
                dto.accountNumber(),
                dto.amount(),
                dto.biNumber(),
                userId
        );
        log.info("Depósito realizado com sucesso - Usuário ID: {}, Conta: {}, Valor: {}",
                userId, dto.accountNumber(), dto.amount());
        return ResponseEntity.ok(
            new ApiResponseDTO<DepositResponse>(
                    true,
                    "Depósito Realizado com sucesso!",
                    newDeposit,
                    null,
                    request.getRequestURI(),
                    OffsetDateTime.now()
            )
        );
    }

    /**
     * Realiza um levantamento sem cartão da conta especificada.
     * - O usuário logado deve ser o dono da conta
     * - Retorna o novo saldo da conta
     */
    @PostMapping("/withdrawals")
    public ResponseEntity<ApiResponseDTO<CardlessWithdrawResponse>> withdraw(
            @Valid @RequestBody CardlessWithdrawRequest dto,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String userId = jwt.getSubject();
        log.debug("Levantamento solicitado por usuário ID:  {} Valor: {}",
                userId, dto.amount());
       CardlessWithdrawResponse withdraw = transactionService.cardlessWithdraw(dto, userId);
        log.info("Levantamento realizado com sucesso - Usuário ID: {},  Valor: {}",
                userId, dto.amount());
        return ResponseEntity.ok(
                new ApiResponseDTO<CardlessWithdrawResponse>(
                        true,
                        "Levantamento Realizado!",
                        withdraw,
                        null,
                        request.getRequestURI(),
                        OffsetDateTime.now()
                )
        );
    }

    @GetMapping("/withdrawals")
    public ResponseEntity<ApiResponseDTO<List<CardlessWithdrawalDetailsResponse>>> listAllCardless(
            @RequestParam(defaultValue = "0", required = true) int page,
            @RequestParam(defaultValue = "10", required = true) int size,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String userId = jwt.getSubject();
        Pageable pageable = PageRequest.of(page, size);
        Page<CardlessWithdrawalDetailsResponse> list = transactionService
                .listCardlessWithdrawalDetails(userId, pageable);

        return  ResponseEntity.ok(
                new ApiResponseDTO<List<CardlessWithdrawalDetailsResponse>>(
                true,
                "Lista de levantamentos sem cartão",
               list.getContent(),
                        new ApiResponseDTO.PaginationMeta(
                       list.getNumber(),
                        list.getSize(),
                        list.getTotalElements(),
                        list.getTotalPages(),
                        list.hasNext(),
                        list.getPageable().hasPrevious()
                        ),
                request.getRequestURI(),
                OffsetDateTime.now()
                ));
    }

    @PostMapping("/withdrawals/{referenceCode}/cancel")
    public  ResponseEntity<Void> cancelWithDrawal(
            @PathVariable String referenceCode,
            @AuthenticationPrincipal Jwt jwt
    ) {
        transactionService.cancelCardlessWithDrawal(referenceCode, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna o histórico completo de transações da conta.
     * - Apenas transações da conta pertencente ao usuário logado
     */

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping()
    public ResponseEntity<ApiResponseDTO<List<TransactionResponse>>> getTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String userId = jwt.getSubject();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TransactionResponse> history = transactionService
                .listTransactions(userId, pageable);
        var response = new ApiResponseDTO<>(
                true,
                "Lista de transaçoes obtidas com sucesso!",
                history.getContent(),
                new ApiResponseDTO.PaginationMeta(
                        history.getNumber(),
                        history.getSize(),
                        history.getTotalElements(),
                        history.getTotalPages(),
                        history.hasNext(),
                        history.hasPrevious()
                ),
                request.getRequestURI(),
                OffsetDateTime.now()
        );
        return ResponseEntity.ok(response);
    }


}