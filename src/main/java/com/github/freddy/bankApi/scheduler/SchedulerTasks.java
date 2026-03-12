package com.github.freddy.bankApi.scheduler;

import com.github.freddy.bankApi.config.FraudProperties;
import com.github.freddy.bankApi.dto.SuspiciousAccount;
import com.github.freddy.bankApi.enums.AccountStatus;
import com.github.freddy.bankApi.repository.AccountRepository;
import com.github.freddy.bankApi.repository.TransactionRepository;
import com.github.freddy.bankApi.service.RefreshTokenService;
import com.github.freddy.bankApi.service.WithdrawalCleanupService;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerTasks {

    private final RefreshTokenService refreshTokenService;
    private final WithdrawalCleanupService withdrawalCleanupService;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final FraudProperties fraudProperties;


    /**
     * Limpeza de refresh tokens expirados
     */
    //@Scheduled(fixedDelay = 300000)
    @Scheduled(fixedDelayString = "${scheduler.fraud-scan}")
    public void cleanExpiredRefreshTokens() {
        log.info("Iniciando limpeza de refresh tokens expirados");

        int deletedCount = refreshTokenService.deleteExpiredTokens();

        log.info("Tokens expirados removidos: {}", deletedCount);
    }

    /**
     * Limpeza de saques cardless expirados
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void cleanExpiredWithdrawals() {
        log.info("Iniciando limpeza de saques cardless expirados - {}", LocalDateTime.now());

        try {
            withdrawalCleanupService.cleanupExpiredWithdrawals();
            log.info("Limpeza concluída com sucesso");
        } catch (Exception e) {
            log.error("Erro durante limpeza de saques expirados", e);
        }
    }

    /**
     * Detecção automática de fraude
     */
    @Scheduled(fixedDelayString = "${scheduler.token-clean}")
    @Transactional
    public void scanForSuspiciousActivity() {

        log.info("Iniciando varredura de fraude...");

        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(fraudProperties.getWindowMinutes());
        BigDecimal highValueThreshold = fraudProperties.getThreshold();

        var suspiciousAccounts = transactionRepository.findSuspiciousAccounts(
                fiveMinutesAgo,
                highValueThreshold,
                fraudProperties.getTransactionCount()
        );

        for (SuspiciousAccount s : suspiciousAccounts) {

            var accountNumber = s.accountNumber();

            var account = accountRepository
                    .findByAccountNumber(accountNumber)
                    .orElse(null);

            if (account != null && account.getStatus() != AccountStatus.BLOCKED) {

                account.setStatus(AccountStatus.BLOCKED);

                log.warn(
                        "ALERTA DE FRAUDE: Conta {} bloqueada por actividade suspeita!",
                        accountNumber
                );
            }
        }
    }
}