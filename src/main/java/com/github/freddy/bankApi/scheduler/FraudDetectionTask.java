package com.github.freddy.bankApi.scheduler;

import com.github.freddy.bankApi.dto.SuspiciousAccount;
import com.github.freddy.bankApi.enums.AccountStatus;
import com.github.freddy.bankApi.repository.AccountRepository;
import com.github.freddy.bankApi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudDetectionTask {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // Roda a cada 5 minutos = 300 000 millsegundos
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void scanForSuspiciousActivity() {
        log.info("Iniciando varredura de fraude...");
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        BigDecimal highValueThreshold = new BigDecimal("1000000.00");

        //Busca IDs de contas que tiveram mais de 3 transações > 1000000 nos últimos 5 min
        var suspiciousAccounts = transactionRepository.findSuspiciousAccounts(
                fiveMinutesAgo,
                highValueThreshold,
                3L
        );
        for (SuspiciousAccount s : suspiciousAccounts) {
            var accountNumber = s.accountNumber();
            var account = accountRepository.findByAccountNumber(accountNumber).orElse(null);

            if (account != null && account.getStatus() != AccountStatus.BLOCKED) {
                //Bloqueia a conta preventivamente
                account.setStatus(AccountStatus.BLOCKED);
                accountRepository.save(account);
                //notificationService.sendSecurityAlert(
                        //account.getUser(),
                        //"Sua conta foi bloqueada preventivamente devido a múltiplas transações de alto valor em curto intervalo."
                //);

                log.warn("ALERTA DE FRAUDE: Conta {} bloqueada por actividade suspeita!", accountNumber);

                // 3. Aqui você poderia disparar um e-mail para o setor de Compliance
            }
        }
    }
}