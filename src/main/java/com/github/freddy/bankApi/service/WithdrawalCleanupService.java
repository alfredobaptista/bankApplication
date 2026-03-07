package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.CardlessWithdrawal;
import com.github.freddy.bankApi.enums.WithdrawalStatus;
import com.github.freddy.bankApi.repository.AccountRepository;
import com.github.freddy.bankApi.repository.CardlessWithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawalCleanupService {

    private final CardlessWithdrawalRepository cardlessRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void cleanupExpiredWithdrawals() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Iniciando limpeza de saques cardless expirados em {}", now);

        // Busca apenas os PENDING expirados
        List<CardlessWithdrawal> expiredWithdrawals = cardlessRepository.findAllExpired(now, WithdrawalStatus.PENDING);

        if (expiredWithdrawals.isEmpty()) {
            log.info("Nenhum saque cardless expirado encontrado.");
        }
        else {
            log.info("Encontrados {} saques cardless expirados para limpeza", expiredWithdrawals.size());

            // Coleta accountNumbers únicos
            Set<String> accountNumbers = expiredWithdrawals.stream()
                    .map(CardlessWithdrawal::getAccountNumber)
                    .collect(Collectors.toSet());

            // Busca contas (com lock pessimista se necessário)
            List<Account> accounts = accountRepository.findAllByAccountNumberIn(accountNumbers);

            // Agrupa o total a devolver por conta (se houver múltiplos saques na mesma conta)
            Map<String, BigDecimal> amountToRefundByAccount = expiredWithdrawals.stream()
                    .collect(Collectors.groupingBy(
                            CardlessWithdrawal::getAccountNumber,
                            Collectors.reducing(BigDecimal.ZERO, CardlessWithdrawal::getAmount, BigDecimal::add)
                    ));

            // Atualiza saldos (devolve o valor reservado)
            int updatedCount = 0;
            for (Account account : accounts) {
                BigDecimal amountToRefund = amountToRefundByAccount.getOrDefault(account.getAccountNumber(), BigDecimal.ZERO);
                if (amountToRefund.compareTo(BigDecimal.ZERO) > 0) {
                    account.setAvailableBalance(account.getAvailableBalance().add(amountToRefund));
                    updatedCount++;
                    log.debug("Devolvendo {} ao saldo da conta {}", amountToRefund, account.getAccountNumber());
                }
            }
            // Salva as contas actualizadas (batch)
            if (!accounts.isEmpty()) {
                accountRepository.saveAll(accounts);
            }
            cardlessRepository.deleteAll(expiredWithdrawals);

            log.info("Limpeza concluída: {} contas atualizadas, {} saques removidos/marcados como expirados",
                    updatedCount, expiredWithdrawals.size());

        }
    }
}