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

        List<CardlessWithdrawal> expiredWithdrawals = buscarSaquesExpirados(now);
        if (expiredWithdrawals.isEmpty()) {
            log.info("Nenhum saque cardless expirado encontrado.");
            return;
        }

        Map<String, BigDecimal> valoresPorConta = agruparValoresPorConta(expiredWithdrawals);
        List<Account> contas = buscarContasRelacionadas(valoresPorConta.keySet());
        int contasAtualizadas = atualizarSaldos(contas, valoresPorConta);

        salvarContas(contas);
        removerSaquesExpirados(expiredWithdrawals);

        log.info("Limpeza concluída: {} contas atualizadas, {} saques removidos",
                contasAtualizadas, expiredWithdrawals.size());
    }

    private List<CardlessWithdrawal> buscarSaquesExpirados(LocalDateTime agora) {
        return cardlessRepository.findAllExpired(agora, WithdrawalStatus.PENDING);
    }

    private Map<String, BigDecimal> agruparValoresPorConta(List<CardlessWithdrawal> saques) {
        return saques.stream()
                .collect(Collectors.groupingBy(
                        CardlessWithdrawal::getAccountNumber,
                        Collectors.reducing(BigDecimal.ZERO, CardlessWithdrawal::getAmount, BigDecimal::add)
                ));
    }

    private List<Account> buscarContasRelacionadas(Set<String> numerosConta) {
        return accountRepository.findAllByAccountNumberIn(numerosConta);
    }

    private int atualizarSaldos(List<Account> contas, Map<String, BigDecimal> valoresPorConta) {
        int atualizadas = 0;
        for (Account conta : contas) {
            BigDecimal valor = valoresPorConta.getOrDefault(conta.getAccountNumber(), BigDecimal.ZERO);
            if (valor.compareTo(BigDecimal.ZERO) > 0) {
                conta.setAvailableBalance(conta.getAvailableBalance().add(valor));
                atualizadas++;
                log.debug("Devolvendo {} ao saldo da conta {}", valor, conta.getAccountNumber());
            }
        }
        return atualizadas;
    }

    private void salvarContas(List<Account> contas) {
        if (!contas.isEmpty()) {
            accountRepository.saveAll(contas);
        }
    }

    private void removerSaquesExpirados(List<CardlessWithdrawal> saques) {
        cardlessRepository.deleteAll(saques);
    }
}
