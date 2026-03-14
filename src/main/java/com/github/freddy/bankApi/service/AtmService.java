package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.dto.request.AtmCardlessRequest;
import com.github.freddy.bankApi.dto.response.AtmCardlessResponse;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.CardlessWithdrawal;
import com.github.freddy.bankApi.entity.Transaction;
import com.github.freddy.bankApi.enums.WithdrawalStatus;
import com.github.freddy.bankApi.exception.InvalidReferenceCodeException;
import com.github.freddy.bankApi.exception.InvalidSecretCodeException;
import com.github.freddy.bankApi.exception.NotFoundException;
import com.github.freddy.bankApi.repository.CardlessWithdrawalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AtmService {

    private final CardlessWithdrawalRepository cardlessRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @Transactional
    public AtmCardlessResponse cardlessWithdraw(AtmCardlessRequest atmRequest) {

        CardlessWithdrawal cw = cardlessRepository
                .findByReferenceCodeAndStatus(atmRequest.referenceCode(), WithdrawalStatus.PENDING)
                .orElseThrow(() -> new NotFoundException("Código de referência inválido"));

        Account account = accountService.getAccountForUpdate(cw.getAccountNumber());

        validateWithdrawal(cw, atmRequest.secretCode(), account);

        accountService.withdraw(account, cw.getAmount());

        Transaction transaction = transactionService.createWithdrawalTransaction(
                account,
                cw.getAmount(),
                cw.getReferenceCode()
        );

        cw.setStatus(WithdrawalStatus.COMPLETED);
        cardlessRepository.save(cw);

        return new AtmCardlessResponse(
                transaction.getId().toString(),
                cw.getAmount(),
                LocalDateTime.now()
        );
    }

    private void validateWithdrawal(CardlessWithdrawal cw, String secretCode, Account account) {
        if (cw.getExpiry().isBefore(LocalDateTime.now())) {
            cw.setStatus(WithdrawalStatus.EXPIRED);
            account.setAvailableBalance(
                    account.getAvailableBalance().add(cw.getAmount())
            );
            cardlessRepository.save(cw);
            throw new InvalidReferenceCodeException("Levantamento expirado");
        }
        if (!cw.getSecretCode().equals(secretCode)) {
            throw new InvalidSecretCodeException("Código inválido");
        }
    }
}
