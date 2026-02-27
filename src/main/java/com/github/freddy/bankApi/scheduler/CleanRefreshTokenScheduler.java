package com.github.freddy.bankApi.scheduler;

import com.github.freddy.bankApi.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Tarefa agendada para limpar refresh tokens expirados da base de dados.
 * Executa diariamente às 3h da manhã (cron ajustável).
 */
@Component
@RequiredArgsConstructor
public class CleanRefreshTokenScheduler {

    private static final Logger log = LoggerFactory.getLogger(CleanRefreshTokenScheduler.class);

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Limpa todos os refresh tokens que expiraram antes da data atual.
     * Executa diariamente às 3:00 AM (horário do servidor).
     */
    @Scheduled(cron = "0 0 3 * * ?")  // Todo dia às 03:00
    public void cleanExpiredRefreshTokens() {
        Instant now = Instant.now();
        log.info("Iniciando limpeza de refresh tokens expirados - Data atual: {}", now);

        int deletedCount = refreshTokenRepository.deleteExpired(now);

        if (deletedCount > 0) {
            log.info("Limpeza concluída: {} refresh tokens expirados removidos", deletedCount);
        } else {
            log.debug("Limpeza concluída: nenhum token expirado encontrado");
        }
    }
}
