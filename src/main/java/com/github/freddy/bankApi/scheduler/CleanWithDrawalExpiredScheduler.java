package com.github.freddy.bankApi.scheduler;

import com.github.freddy.bankApi.service.WithdrawalCleanupService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CleanWithDrawalExpiredScheduler {

    private static final Logger log = LoggerFactory.getLogger(CleanWithDrawalExpiredScheduler.class);

    private final WithdrawalCleanupService cleanupService;

    @Scheduled(cron = "0 */5 * * * ?")  // 5 minutos
    public void cleanExpiredWithdrawals() {
        log.info("Iniciando limpeza de saques cardless expirados - {}", LocalDateTime.now());
        try {
            cleanupService.cleanupExpiredWithdrawals();
            log.info("Limpeza concluída com sucesso");
        } catch (Exception e) {
            log.error("Erro durante limpeza de saques expirados", e);
        }
    }
}