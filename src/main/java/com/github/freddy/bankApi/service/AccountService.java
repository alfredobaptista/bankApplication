package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.dto.response.AccountResponse;
import com.github.freddy.bankApi.dto.response.BalanceResponse;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.User;
import com.github.freddy.bankApi.enums.AccountStatus;
import com.github.freddy.bankApi.enums.AccountType;
import com.github.freddy.bankApi.exception.AccountCreationException;
import com.github.freddy.bankApi.exception.InsufficientBalanceException;
import com.github.freddy.bankApi.exception.InvalidAccountStatusException;
import com.github.freddy.bankApi.exception.NotFoundException;
import com.github.freddy.bankApi.mapper.AccountMapper;
import com.github.freddy.bankApi.repository.AccountRepository;
import com.github.freddy.bankApi.util.AccountNumberUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Serviço responsável por operações relacionadas a contas bancárias.
 */
@Service
@RequiredArgsConstructor
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    /**
     * Cria uma conta padrão para o utilizador.
     * - Saldo inicial = 0,00
     * - Status = ATIVE
     * - Moeda = AOA
     * - Número gerado via sequence + utilitário
     */

    @Transactional
    public AccountResponse createDefaultAccount(User user, AccountType typeAccount) {
        log.info("Criando conta padrão para utilizador: {} (ID: {}) - Tipo: {}", user.getName(), user.getId(), typeAccount);
        Account newAccount = accountMapper.toEntity(user, typeAccount);
        // Gera número único
        Long sequenceNumber = accountRepository.getNextSequenceValue();
        String accountNumber = AccountNumberUtil.generateAccountNumber(sequenceNumber);
        // Verificação extra de unicidade
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            log.error("Conflito inesperado: número de conta {} já existe", accountNumber);
            throw new AccountCreationException(
                    "Estamos com um pequeno problema ao criar o número da sua conta. " +
                            "Por favor, tente novamente em alguns segundos. " +
                            "Se o problema persistir, contacte o suporte."
            );
        }
        newAccount.setAccountNumber(accountNumber);
        // Salva com flush para garantir ID imediato
        Account savedAccount = accountRepository.saveAndFlush(newAccount);
        log.info(
                "Conta criada com sucesso: número={}, tipo={}, utilizador={}, saldo={}",
                savedAccount.getAccountNumber(), savedAccount.getAccountType(),
                user.getName(), savedAccount.getLedgerBalance()
        );
        return accountMapper.toResponse(savedAccount);
    }

    /**
     * Consulta o saldo actual da conta.
     */
    public BalanceResponse getBalance(String userId) {
        var account = accountRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new NotFoundException(
                        "Conta não encontrada"));
       return accountMapper.toBalanceResponse(account);
    }

    /**
     * Retorna os dados completos da conta mapeados para DTO.
     */
    public AccountResponse getAccount( String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException(
                        "Conta com número  não encontrada"));
        return accountMapper.toResponse(account);
    }

    public Account getAccountForUpdate(String accountNumber) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new NotFoundException("Conta não encontrada"));
    }

    public void withdraw(Account account, BigDecimal amount) {

        if (account.getLedgerBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }

        account.setLedgerBalance(account.getLedgerBalance().subtract(amount));
    }

    /**
     * Altera o status da conta (com lock pessimista para evitar concorrência).
     */
    @Transactional
    public void changeStatusAccount(String accountNumber, AccountStatus newStatus) {

        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new NotFoundException(
                        "Conta com número " + accountNumber + " não encontrada"));

        log.info("Alterando status da conta {} de {} para {}",
                accountNumber, account.getStatus(), newStatus);

        if (account.getStatus() == newStatus) {
            return;
        }

        if (account.getStatus() == AccountStatus.BLOCKED && newStatus != AccountStatus.ACTIVE) {
            throw new InvalidAccountStatusException(
                    "Conta bloqueada só pode ser reativada para ACTIVE");
        }

        account.setStatus(newStatus);

        log.info("Status da conta {} alterado com sucesso para {}", accountNumber, newStatus);
    }
}

