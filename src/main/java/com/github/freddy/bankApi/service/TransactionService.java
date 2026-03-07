package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.dto.request.CardlessWithdrawRequest;
import com.github.freddy.bankApi.dto.response.*;
import com.github.freddy.bankApi.dto.request.TransferRequest;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.CardlessWithdrawal;
import com.github.freddy.bankApi.entity.Transaction;
import com.github.freddy.bankApi.enums.TransactionStatus;
import com.github.freddy.bankApi.enums.TransactionType;
import com.github.freddy.bankApi.enums.WithdrawalStatus;
import com.github.freddy.bankApi.exception.ConflictException;
import com.github.freddy.bankApi.exception.InsufficientBalanceException;
import com.github.freddy.bankApi.exception.NotFoundException;
import com.github.freddy.bankApi.exception.UnauthorizedException;
import com.github.freddy.bankApi.mapper.TransactionMapper;
import com.github.freddy.bankApi.repository.AccountRepository;
import com.github.freddy.bankApi.repository.CardlessWithdrawalRepository;
import com.github.freddy.bankApi.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("120000.00");


    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final CardlessWithdrawalRepository cardlessRepository;

    /**
     * Realiza uma transferência entre contas.
     * Valida propriedade da conta de origem.
     */
    @Transactional
    public TransferResponse transfer(TransferRequest dto, String userId) {
        log.info("Transferência solicitada - Destino: {}, Valor: {}, Usuário ID: {}", dto.accountNumber(), dto.amount(), userId);

        Account source = accountRepository.findByUserIdForUpdate(UUID.fromString(userId))
                .orElseThrow(() -> new NotFoundException("Conta de origem não encontrada"));

        Account destination = accountRepository.findByAccountNumberForUpdate(dto.accountNumber())
                .orElseThrow(() -> new NotFoundException("Número de conta inválido"));

        // Segurança: verifica se a conta de origem pertence ao usuário logado
        if (!source.getUser().getId().toString().equals(userId)) {
            log.warn("Tentativa não autorizada de transferência na conta {}", source.getAccountNumber());
            throw new UnauthorizedException("Você não tem permissão para operar esta conta");
        }
        validateTransfer(source, dto.amount(), dto.accountNumber());
        source.setLedgerBalance(source.getLedgerBalance().subtract(dto.amount()));
        source.setAvailableBalance(source.getAvailableBalance().subtract(dto.amount()));

        destination.setLedgerBalance(destination.getLedgerBalance().add(dto.amount()));
        destination.setAvailableBalance(destination.getAvailableBalance().add(dto.amount()));

        accountRepository.save(source);
        accountRepository.save(destination);

        var savedTransaction = saveTransaction(
                destination, source, dto.amount(), TransactionType.TRANSFER,
                TransactionStatus.COMPLETED,
                "Transferência realizada com sucesso");
        log.info("Transferência concluída - Novo saldo origem: {}", source.getAvailableBalance());
        return transactionMapper.toResponse(savedTransaction, source, destination, dto.amount());
    }


    /**
     * Realiza um depósito na conta.
     * Valida propriedade da conta.
     */
    @Transactional
    public DepositResponse deposit(
            String accountNumber,
            BigDecimal amount,
            String biNumber,
            String userId
    ) {
        log.info("Depósito solicitado - Conta: {}, Valor: {}, Funcionario ID: {}", accountNumber, amount, userId);
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new NotFoundException("Conta não encontrada"));
        if (!account.getUser().getBi().equals(biNumber)) {
            log.warn("Tentativa não autorizada de depósito na conta {}", accountNumber);
            throw new UnauthorizedException("Você não tem permissão para operar esta conta");
        }
        account.setLedgerBalance(account.getLedgerBalance().add(amount));
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        Account updated = accountRepository.save(account);
        var transaction = saveTransaction(account, null, amount,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                "Depósito em numerário");
        log.info("Depósito concluído - Novo saldo: {}", updated.getLedgerBalance());
        return new DepositResponse(
                transaction.getId(),
                account.getAccountNumber(),
                account.getUser().getName(),
                amount,
                account.getLedgerBalance(),
                account.getCurrencyCode(),
                transaction.getStatus(),
                transaction.getDescription()
        );
    }

    /**
     * Lista o histórico de transações da conta.
     * Valida propriedade da conta.
     */
    public Page<TransactionResponse> listTransactions(
            String userId,
            Pageable pageable
    ) {
        Page<Transaction> transactions =
                transactionRepository.findByUserId(
                        UUID.fromString(userId),
                        pageable
                );
        return transactions.map(transactionMapper::toResponse);
    }

    // Levantamento sem cartao
    @Transactional
    public CardlessWithdrawResponse cardlessWithdraw(
            CardlessWithdrawRequest cardDto,
            String userId
    ) {
        Account account = accountRepository
                .findByUserIdForUpdate(UUID.fromString(userId))
                .orElseThrow(() -> new NotFoundException("Conta não encontrada"));

        if (account.getAvailableBalance().compareTo(cardDto.amount()) < 0) {
            throw new InsufficientBalanceException("Saldo disponivel insuficiente");
        }
        String referenceCode = String.format(
                "%08d", new Random().nextInt(100000000)
        );
        CardlessWithdrawal cw = new CardlessWithdrawal();
        cw.setUserId(UUID.fromString(userId));
        cw.setAccountNumber(account.getAccountNumber());
        cw.setAmount(cardDto.amount());
        cw.setReferenceCode(referenceCode);
        cw.setSecretCode(cardDto.secretCode());
        cw.setStatus(WithdrawalStatus.PENDING);

        //Durçao de validdae do codigo
        cw.setExpiry(LocalDateTime.now().plusMinutes(3));
        cw = cardlessRepository.save(cw);

        // Reserva o valor temporariamente
        account.setAvailableBalance(
                account.getAvailableBalance().subtract(cardDto.amount())
        );
        accountRepository.save(account);
        saveTransaction(
                null,
                account,
                cardDto.amount(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.PENDING,
                "Levantamento sem cartão"
        );
        return new CardlessWithdrawResponse(cw.getReferenceCode(),  cw.getAmount());
    }

    public void cancelCardlessWithDrawal(Long cwId, String userId) {
        var withdrawal = cardlessRepository
                .findById(cwId)
                .orElseThrow(
                        () -> new NotFoundException("Levantamento não existente")
                );
        var account = accountRepository
                .findByUserIdForUpdate(UUID.fromString(userId))
                .orElseThrow(() -> new NotFoundException("Levantamneto não existente"));
        withdrawal.setStatus(WithdrawalStatus.CANCELLED);
        cardlessRepository.save(withdrawal);
        account.setAvailableBalance(account.getAvailableBalance().add(withdrawal.getAmount()));
    }

    // Validações para transferência
    private void validateTransfer(Account source, BigDecimal amount, String destinationNumber) {
        if (source.getAccountNumber().equals(destinationNumber)) {
            throw new ConflictException("Não é permitido transferir para a própria conta");
        }

        if (source.getLedgerBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }
        // Limite diário (exemplo simples - podes melhorar com soma real do dia)
        if (amount.compareTo(DAILY_LIMIT) > 0) {
            throw new IllegalStateException("Valor excede limite diário de transferência");
        }
    }

    // Salva transação
    private Transaction saveTransaction(
            Account destination, Account source, BigDecimal amount,
            TransactionType type, TransactionStatus status, String description
    ) {
        Transaction tx = Transaction.builder()
                .sourceAccount(source)
                .destinationAccount(destination)
                .amount(amount.setScale(2, RoundingMode.HALF_UP))
                .type(type)
                .status(status)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(tx);
    }
}