package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.Transaction;
import com.github.freddy.bankApi.enums.TransactionStatus;
import com.github.freddy.bankApi.enums.TransactionType;
import com.github.freddy.bankApi.exception.InsufficientBalanceException;
import com.github.freddy.bankApi.exception.ResourceNotFoundException;
import com.github.freddy.bankApi.exception.UnauthorizedAccessException;
import com.github.freddy.bankApi.repository.AccountRepository;
import com.github.freddy.bankApi.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço responsável por operações de transações bancárias.
 * Valida propriedade da conta com base no userId do token JWT.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private static final BigDecimal DAILY_LIMIT = new BigDecimal("120000.00");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Realiza uma transferência entre contas.
     * Valida propriedade da conta de origem.
     */
    @Transactional
    public BigDecimal transfer(String sourceNumber, BigDecimal amount, String destinationNumber, String userId) {
        log.info("Transferência solicitada - Origem: {}, Destino: {}, Valor: {}, Usuário ID: {}",
                sourceNumber, destinationNumber, amount, userId);

        Account source = accountRepository.findByAccountNumberForUpdate(sourceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Conta de origem não encontrada"));

        Account destination = accountRepository.findByAccountNumberForUpdate(destinationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Conta de destino não encontrada"));

        // Segurança: verifica se a conta de origem pertence ao usuário logado
        if (!source.getUser().getId().toString().equals(userId)) {
            log.warn("Tentativa não autorizada de transferência na conta {}", sourceNumber);
            throw new UnauthorizedAccessException("Você não tem permissão para operar esta conta");
        }

        validateTransfer(source, amount, destinationNumber);

        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        accountRepository.save(source);
        accountRepository.save(destination);

        saveTransaction(destination, source, amount, TransactionType.TRANSFER, TransactionStatus.COMPLETED,
                "Transferência concluída com sucesso");

        log.info("Transferência concluída - Novo saldo origem: {}", source.getBalance());

        return source.getBalance();
    }


    // Versão para uso interno (ex: tasks agendadas, sem validação de user logado)
    @Transactional
    public BigDecimal deposit(String accountNumber, BigDecimal amount) {
        log.info("Depósito interno (task) - Conta: {}, Valor: {}", accountNumber, amount);

        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        account.setBalance(account.getBalance().add(amount));
        Account updated = accountRepository.save(account);

        saveTransaction(account, null, amount, TransactionType.DEPOSIT, TransactionStatus.COMPLETED,
                "Depósito automático (juros ou task)");

        return updated.getBalance();
    }
    /**
     * Realiza um depósito na conta.
     * Valida propriedade da conta.
     */
    @Transactional
    public BigDecimal deposit(String accountNumber, BigDecimal amount, String userId) {
        log.info("Depósito solicitado - Conta: {}, Valor: {}, Usuário ID: {}", accountNumber, amount, userId);

        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        if (!account.getUser().getId().toString().equals(userId)) {
            log.warn("Tentativa não autorizada de depósito na conta {}", accountNumber);
            throw new UnauthorizedAccessException("Você não tem permissão para operar esta conta");
        }

        account.setBalance(account.getBalance().add(amount));
        Account updated = accountRepository.save(account);

        saveTransaction(account, null, amount, TransactionType.DEPOSIT, TransactionStatus.COMPLETED,
                "Depósito em numerário");

        log.info("Depósito concluído - Novo saldo: {}", updated.getBalance());

        return updated.getBalance();
    }

    /**
     * Realiza um levantamento (saque) da conta.
     * Valida propriedade da conta.
     */
    @Transactional
    public BigDecimal withdraw(String accountNumber, BigDecimal amount, String userId) {
        log.info("Levantamento solicitado - Conta: {}, Valor: {}, Usuário ID: {}", accountNumber, amount, userId);

        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        if (!account.getUser().getId().toString().equals(userId)) {
            log.warn("Tentativa não autorizada de levantamento na conta {}", accountNumber);
            throw new UnauthorizedAccessException("Você não tem permissão para operar esta conta");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente para levantamento");
        }

        account.setBalance(account.getBalance().subtract(amount));
        Account updated = accountRepository.save(account);

        saveTransaction(null, account, amount, TransactionType.WITHDRAWAL, TransactionStatus.COMPLETED,
                "Levantamento em ATM");

        log.info("Levantamento concluído - Novo saldo: {}", updated.getBalance());

        return updated.getBalance();
    }

    /**
     * Lista o histórico de transações da conta.
     * Valida propriedade da conta.
     */
    public List<Transaction> listAllTransactions(String accountNumber, String userId) {
        log.debug("Consulta de histórico - Conta: {}, Usuário ID: {}", accountNumber, userId);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        if (!account.getUser().getId().toString().equals(userId)) {
            log.warn("Tentativa não autorizada de ver histórico da conta {}", accountNumber);
            throw new UnauthorizedAccessException("Você não tem permissão para ver o histórico desta conta");
        }

        List<Transaction> history = transactionRepository.findAllByAccountNumber(accountNumber);

        log.info("Histórico retornado - Conta: {}, Total de transações: {}", accountNumber, history.size());

        return history;
    }

    // Validações para transferência
    private void validateTransfer(Account source, BigDecimal amount, String destinationNumber) {
        if (source.getAccountNumber().equals(destinationNumber)) {
            throw new IllegalArgumentException("Não é permitido transferir para a própria conta");
        }

        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }

        // Limite diário (exemplo simples - podes melhorar com soma real do dia)
        if (amount.compareTo(DAILY_LIMIT) > 0) {
            throw new IllegalStateException("Valor excede limite diário de transferência");
        }
    }

    // Salva transação
    private void saveTransaction(Account destination, Account source, BigDecimal amount,
                                 TransactionType type, TransactionStatus status, String description) {
        Transaction tx = Transaction.builder()
                .sourceAccount(source)
                .destinationAccount(destination)
                .amount(amount.setScale(2, RoundingMode.HALF_UP))
                .type(type)
                .status(status)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(tx);
    }
}