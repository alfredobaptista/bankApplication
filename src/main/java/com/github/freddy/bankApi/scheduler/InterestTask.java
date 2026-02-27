package com.github.freddy.bankApi.scheduler;

import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.enums.AccountStatus;
import com.github.freddy.bankApi.enums.AccountType;
import com.github.freddy.bankApi.repository.AccountRepository;
import com.github.freddy.bankApi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterestTask {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;

    /**
     * Roda à meia-noite do dia 1 de cada mês.
     * Cron: "0 0 0 1 * ?"
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void applyMonthlyInterest() {
        log.info("Iniciando o processamento automático de juros mensais...");

        // 1. Busca apenas contas POUPANÇA que estejam ATIVAS
        // Melhor filtrar no banco para poupar memória e performance
        List<Account> savingsAccounts = accountRepository.findByAccountTypeAndStatus(
                AccountType.SAVINGS, AccountStatus.ACTIVE);

        // 2. Taxa de juros (0.5% ao mês)
        BigDecimal interestRate = new BigDecimal("0.005");

        for (Account account : savingsAccounts) {
            BigDecimal interest = account.getBalance().multiply(interestRate);

            if (interest.compareTo(BigDecimal.ZERO) > 0) {
                try {
                    // Chamamos o service para garantir que a lógica de depósito
                    // e auditoria de transação seja executada corretamente
                    transactionService.deposit(account.getAccountNumber(), interest);
                    log.info("Juros de {} aplicados com sucesso na conta {}", interest, account.getAccountNumber());
                } catch (Exception e) {
                    log.error("Erro ao aplicar juros na conta {}: {}", account.getAccountNumber(), e.getMessage());
                }
            }
        }

        log.info("Processamento de juros finalizado com sucesso.");
    }
}