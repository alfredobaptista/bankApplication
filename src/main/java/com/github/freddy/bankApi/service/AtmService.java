package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.dto.request.AtmCompleteCardlessRequest;
import com.github.freddy.bankApi.dto.response.AtmCompleteResponse;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.CardlessWithdrawal;
import com.github.freddy.bankApi.entity.Transaction;
import com.github.freddy.bankApi.enums.TransactionStatus;
import com.github.freddy.bankApi.enums.TransactionType;
import com.github.freddy.bankApi.enums.WithdrawalStatus;
import com.github.freddy.bankApi.exception.InsufficientBalanceException;
import com.github.freddy.bankApi.exception.InvalidReferenceCodeException;
import com.github.freddy.bankApi.exception.NotFoundException;
import com.github.freddy.bankApi.repository.AccountRepository;
import com.github.freddy.bankApi.repository.CardlessWithdrawalRepository;
import com.github.freddy.bankApi.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtmService {

    private final CardlessWithdrawalRepository cardlessRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Simula o processamento de um levantamento sem cartão diretamente no ATM.
     * Executado pelo "ATM" .
     */
    @Transactional
    public AtmCompleteResponse completeCardlessWithdrawAtAtm(AtmCompleteCardlessRequest atmRequest) {
        log.info(
                "ATM - Iniciando processamento cardless | Referência: {}, " +
                        "Código secreto fornecido: [protegido]",
                atmRequest.referenceCode()
        );

        // 1. Busca o pedido pendente
        CardlessWithdrawal cw = cardlessRepository.findByReferenceCodeAndStatus(
                        atmRequest.referenceCode(), WithdrawalStatus.PENDING)
                .orElseThrow(() -> new NotFoundException("Código de referência inválido"));

        //Busca conta com lock pessimista para evitar race conditions
        Account account = accountRepository.findByAccountNumberForUpdate(cw.getAccountNumber())
                .orElseThrow(() -> new NotFoundException("Conta associada não encontrada"));

        validateWithdrawal(cw, atmRequest.secretCode(), account);

        if (account.getAvailableBalance().compareTo(cw.getAmount()) < 0) {
            cw.setStatus(WithdrawalStatus.FAILED);
            cardlessRepository.save(cw);
            log.warn("Saldo disponível insuficiente | Conta: {}, Solicitado: {}, Disponível: {}",
                    account.getAccountNumber(),
                    cw.getAmount(),
                    account.getAvailableBalance());
            throw new InsufficientBalanceException("Saldo disponível insuficiente no momento do levantamento");
        }
        BigDecimal amount = cw.getAmount();
        account.setLedgerBalance(account.getLedgerBalance().subtract(amount));
        accountRepository.save(account);

        // Regista a transação com detalhes do ATM
        Transaction transaction = Transaction.builder()
                .sourceAccount(account)
                .destinationAccount(null)
                .amount(amount)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .description("Levantamento sem cartão via ATM | Ref: " + cw.getReferenceCode())
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        // Finaliza o pedido cardless
        cw.setStatus(WithdrawalStatus.COMPLETED);
        cardlessRepository.save(cw);

        log.info(
                "ATM - Levantamento concluído com sucesso | Ref: {}, Valor: {}, Nova disponível: {}",
                cw.getReferenceCode(), amount, account.getAvailableBalance());
        return new AtmCompleteResponse(
                transaction.getId().toString(),
                amount
        );
    }

    private void validateWithdrawal(CardlessWithdrawal cw, String secretCode, Account account) {
        // Verifica expiração
        if (cw.getExpiry().isBefore(LocalDateTime.now())) {
            cw.setStatus(WithdrawalStatus.EXPIRED);
            account.setAvailableBalance(account.getAvailableBalance().add(cw.getAmount()));
            cardlessRepository.save(cw);
            log.warn("Tentativa de uso de código expirado | Ref: {}", cw.getReferenceCode());
            throw new IllegalStateException("Código expirado");
        }
        //  Valida código secreto
        if (!cw.getSecretCode().equals(secretCode)) {
            log.warn("Código secreto incorreto para ref: {}", cw.getReferenceCode());
            throw new InvalidReferenceCodeException("Código secreto inválido");
        }
    }
}
