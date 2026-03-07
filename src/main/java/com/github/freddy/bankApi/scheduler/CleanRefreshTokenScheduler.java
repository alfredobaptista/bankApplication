package com.github.freddy.bankApi.scheduler;

import com.github.freddy.bankApi.repository.RefreshTokenRepository;
import com.github.freddy.bankApi.service.RefreshTokenService;
import jakarta.transaction.Transactional;
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
    private final RefreshTokenService refreshTokenService;
    /**
     * Limpa todos os refresh tokens que expiraram antes da data atual.
     * Executa diariamente às 3:00 AM (horário do servidor).
     */
    @Scheduled(fixedDelay = 300000)
    public void cleanExpiredRefreshTokens() {
        log.info("Iniciando limpeza de refresh tokens expirados");
        int deletedCount = refreshTokenService.deleteExpiredTokens();
        log.info("Tokens expirados removidos: {}", deletedCount);
    }
}
