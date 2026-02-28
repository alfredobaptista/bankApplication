package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.dto.request.DepositRequest;
import com.github.freddy.bankApi.dto.request.TransferRequest;
import com.github.freddy.bankApi.dto.request.WithdrawRequest;
import com.github.freddy.bankApi.dto.response.TransferResponse;
import com.github.freddy.bankApi.entity.Transaction;
import com.github.freddy.bankApi.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller para operações de transações bancárias.
 * Todos os endpoints exigem autenticação JWT e verificam propriedade da conta.
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    /**
     * Realiza uma transferência entre contas.
     * - O usuário logado deve ser o dono da conta de origem
     * - Retorna o novo saldo da conta de origem
     */
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        log.debug("Transferência solicitada por usuário ID: {} - Para: {}  Valor: {}",
                userId, dto.accountNumber(), dto.amount());

        TransferResponse response = transactionService.transfer(dto, userId);
        log.info("Transferência realizada com sucesso - Usuário ID: {}, Para: {}, Valor: {}",
                userId,  dto.accountNumber(), dto.amount());
        return ResponseEntity.ok(response);
    }

    /**
     * Realiza um depósito na conta especificada.
     * - O usuário logado deve ser o dono da conta
     * - Retorna o novo saldo da conta
     */
    @PostMapping("/deposit")
    public ResponseEntity<BigDecimal> deposit(
            @Valid @RequestBody DepositRequest dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        log.debug("Depósito solicitado por usuário ID: {} - Conta: {} Valor: {}",
                userId, dto.accountNumber(), dto.amount());

        BigDecimal newBalance = transactionService.deposit(
                dto.accountNumber(),
                dto.amount(),
                userId
        );

        log.info("Depósito realizado com sucesso - Usuário ID: {}, Conta: {}, Valor: {}",
                userId, dto.accountNumber(), dto.amount());

        return ResponseEntity.ok(newBalance);
    }

    /**
     * Realiza um levantamento (saque) da conta especificada.
     * - O usuário logado deve ser o dono da conta
     * - Retorna o novo saldo da conta
     */
    @PostMapping("/withdraw")
    public ResponseEntity<BigDecimal> withdraw(
            @Valid @RequestBody WithdrawRequest dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        log.debug("Levantamento solicitado por usuário ID: {} - Conta: {} Valor: {}",
                userId, dto.accountNumber(), dto.amount());

        BigDecimal newBalance = transactionService.withdraw(
                dto.accountNumber(),
                dto.amount(),
                userId
        );

        log.info("Levantamento realizado com sucesso - Usuário ID: {}, Conta: {}, Valor: {}",
                userId, dto.accountNumber(), dto.amount());

        return ResponseEntity.ok(newBalance);
    }

    /**
     * Retorna o histórico completo de transações da conta.
     * - Apenas transações da conta pertencente ao usuário logado
     */
    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<List<Transaction>> getHistory(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        log.debug("Consulta de histórico solicitada por usuário ID: {} - Conta: {}", userId, accountNumber);

        List<Transaction> history = transactionService.listAllTransactions(accountNumber, userId);

        log.info("Histórico retornado com sucesso - Conta: {}, Total de transações: {}",
                accountNumber, history.size());

        return ResponseEntity.ok(history);
    }
}