package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.config.CardlessConfig;
import com.github.freddy.bankApi.dto.request.CardlessWithdrawRequest;
import com.github.freddy.bankApi.dto.response.*;
import com.github.freddy.bankApi.dto.request.TransferRequest;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.CardlessWithdrawal;
import com.github.freddy.bankApi.entity.Transaction;
import com.github.freddy.bankApi.enums.AccountStatus;
import com.github.freddy.bankApi.enums.TransactionStatus;
import com.github.freddy.bankApi.enums.TransactionType;
import com.github.freddy.bankApi.enums.WithdrawalStatus;
import com.github.freddy.bankApi.exception.*;
import com.github.freddy.bankApi.mapper.CardLessMapper;
import com.github.freddy.bankApi.mapper.TransactionMapper;
import com.github.freddy.bankApi.repository.AccountRepository;
import com.github.freddy.bankApi.repository.CardlessWithdrawalRepository;
import com.github.freddy.bankApi.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final CardlessWithdrawalRepository cardlessRepository;
    private final CardLessMapper cardLessMapper;

    private final CardlessConfig config;

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
            throw new UnauthorizedException("Número de bilhete inválido");
        }

        if(account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Tentativa de deposito em conta não activada");
            throw new UnauthorizedException("Conta não esta activada");
        }

        account.setLedgerBalance(account.getLedgerBalance().add(amount));
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        Account updated = accountRepository.save(account);

        var transaction = saveTransaction(null, account, amount,
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

    public Page<CardlessWithdrawalDetailsResponse> listCardlessWithdrawalDetails(String userId,  Pageable pageable) {
        Page<CardlessWithdrawal> cardlessWithdrawals = cardlessRepository.findAllByUserId(pageable, UUID.fromString(userId));
        return cardlessWithdrawals.map(cardLessMapper::toResponse);
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
        ensureSufficientFunds(account.getAvailableBalance(), cardDto.amount());
        String referenceCode = generatedReferenceCode();
        CardlessWithdrawal cw = cardLessMapper.toEntity(account,cardDto,UUID.fromString(userId), referenceCode);
        //Durçao de validdae do codigo
        cw.setExpiry(LocalDateTime.now().plusMinutes(config.getExpiryTime()));
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

    public Transaction createWithdrawalTransaction(
            Account account,
            BigDecimal amount,
            String reference
    ) {

        Transaction transaction = Transaction.builder()
                .sourceAccount(account)
                .amount(amount)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .description("Levantamento ATM | Ref: " + reference)
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }

    //Cancelar um levantamento sem cartao
    @Transactional
    public void cancelCardlessWithDrawal(String referenceCode, String userId) {
        var withdrawal = cardlessRepository
                .findByReferenceCode(referenceCode)
                .orElseThrow(
                        () -> new NotFoundException("Levantamento não existente")
                );
        var account = accountRepository
                .findByUserIdForUpdate(UUID.fromString(userId))
                .orElseThrow(() -> new NotFoundException("Levantamento não existente"));
        withdrawal.setStatus(WithdrawalStatus.CANCELLED);
        cardlessRepository.save(withdrawal);
        account.setAvailableBalance(account.getAvailableBalance().add(withdrawal.getAmount()));
        accountRepository.save(account);
    }


    // Validações para transferência, verifc
    private void validateTransfer(Account source, BigDecimal amount, String destinationNumber) {
        if (source.getAccountNumber().equals(destinationNumber)) {
            throw new ConflictException("Não é permitido transferir para a própria conta");
        }

        if (source.getLedgerBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }
    }

    // Cria e salva uma nova transaçao no banco
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


    //Gera o codigo de refrencia com ate 8 digitos
    private String generatedReferenceCode() {
        return String.format(
                "%08d", new Random().nextInt(100000000)
        );
    }

    //Verifica se ytem saldo suficiente na conta
    private void ensureSufficientFunds(BigDecimal currentBalance, BigDecimal withdrawalAmount) {
        if (currentBalance.compareTo(withdrawalAmount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }
    }

}