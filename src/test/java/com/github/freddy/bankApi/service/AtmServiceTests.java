package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.dto.request.AtmCardlessRequest;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.CardlessWithdrawal;
import com.github.freddy.bankApi.entity.Transaction;
import com.github.freddy.bankApi.entity.User;
import com.github.freddy.bankApi.enums.WithdrawalStatus;
import com.github.freddy.bankApi.exception.InvalidReferenceCodeException;
import com.github.freddy.bankApi.exception.InvalidSecretCodeException;
import com.github.freddy.bankApi.exception.NotFoundException;
import com.github.freddy.bankApi.factory.*;
import com.github.freddy.bankApi.repository.CardlessWithdrawalRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AtmServiceTests {

    @Mock private AccountService accountService;
    @Mock private TransactionService transactionService;
    @Mock private CardlessWithdrawalRepository cardlessWithdrawalRepository;

    @InjectMocks private AtmService atmService;

    @Nested
    class CardlessWithdraw {

        @Test
        public void cardlessWithdrawSucessfuly() {
            // ARRANGE
            User user = UserFactory.createUser();
            Account account = AccountFactory.createAccount(user);
            CardlessWithdrawal cw = CardlessWithdrawalFactory.createApendingCardlessWithdrawal(account);

            Transaction transaction = TransactionFactory.aCompletedTransaction(
                    account, cw.getAmount(), CardlessWithdrawalFactory.VALID_REFERENCE);

            doReturn(Optional.of(cw))
                    .when(cardlessWithdrawalRepository)
                    .findByReferenceCodeAndStatus(CardlessWithdrawalFactory.VALID_REFERENCE, WithdrawalStatus.PENDING);

            doReturn(account)
                    .when(accountService)
                    .getAccountForUpdate(cw.getAccountNumber());

            doNothing()
                    .when(accountService)
                    .withdraw(account, cw.getAmount());

            doReturn(transaction)
                    .when(transactionService)
                    .createWithdrawalTransaction(account, cw.getAmount(), cw.getReferenceCode());

            cw.setStatus(WithdrawalStatus.COMPLETED);

            doReturn(cw)
                    .when(cardlessWithdrawalRepository)
                    .save(cw);

            // ACT
            var response = atmService.cardlessWithdraw(CardlessRequestFactory.createAtmCardlessRequest());

            // ASSERT
            assertNotNull(response);
            assertEquals(cw.getAmount(), response.amountWithdrawn());

            // VERIFICATIONS
            verify(accountService, times(1)).getAccountForUpdate(cw.getAccountNumber());
            verify(accountService).withdraw(account, cw.getAmount());
            verify(transactionService).createWithdrawalTransaction(account, cw.getAmount(), cw.getReferenceCode());
            verify(cardlessWithdrawalRepository).findByReferenceCodeAndStatus(
                    CardlessWithdrawalFactory.VALID_REFERENCE, WithdrawalStatus.PENDING);
            verify(cardlessWithdrawalRepository).save(argThat(w -> w.getStatus() == WithdrawalStatus.COMPLETED));
        }

        @Test
        public void cardlessWithdrawWithInvalidReferenceCode() {
            // ARRANGE
            doReturn(Optional.empty())
                    .when(cardlessWithdrawalRepository)
                    .findByReferenceCodeAndStatus(CardlessWithdrawalFactory.INVALID_REFERENCE, WithdrawalStatus.PENDING);

            // ACT + ASSERT
            assertThrows(NotFoundException.class,
                    () -> atmService.cardlessWithdraw(CardlessRequestFactory.createAtmCardlessRequestInvalidReferenceCode()));

            // VERIFICATIONS
            verify(cardlessWithdrawalRepository, times(1))
                    .findByReferenceCodeAndStatus(CardlessWithdrawalFactory.INVALID_REFERENCE, WithdrawalStatus.PENDING);

            verifyNoInteractions(accountService);
            verifyNoInteractions(transactionService);
            verify(cardlessWithdrawalRepository, never()).save(any());
        }

        @Test
        public void cardlessWithdrawWithInvalidSecretCode() {
            // ARRANGE
            User user = UserFactory.createUser();
            Account account = AccountFactory.createAccount(user);
            CardlessWithdrawal cw = CardlessWithdrawalFactory.createApendingCardlessWithdrawal(account);

            doReturn(Optional.of(cw))
                    .when(cardlessWithdrawalRepository)
                    .findByReferenceCodeAndStatus(CardlessWithdrawalFactory.VALID_REFERENCE, WithdrawalStatus.PENDING);

            doReturn(account)
                    .when(accountService)
                    .getAccountForUpdate(cw.getAccountNumber());

            // ACT + ASSERT
            assertThrows(InvalidSecretCodeException.class,
                    () -> atmService.cardlessWithdraw(CardlessRequestFactory.createAtmCardlessRequestInvalidSecretCode()));

            // VERIFICATIONS
            verify(cardlessWithdrawalRepository).findByReferenceCodeAndStatus(
                    CardlessWithdrawalFactory.VALID_REFERENCE, WithdrawalStatus.PENDING);

            verify(accountService).getAccountForUpdate(cw.getAccountNumber());

            verify(accountService, never()).withdraw(any(), any());
            verify(transactionService, never()).createWithdrawalTransaction(any(), any(), any());
            verify(cardlessWithdrawalRepository, never()).save(any());

            verifyNoMoreInteractions(cardlessWithdrawalRepository);
            verifyNoMoreInteractions(transactionService);
        }

        @Test
        public void cardlessWithdrawWithExpiredWithdrawal() {
            // ARRANGE
            User user = UserFactory.createUser();
            Account account = AccountFactory.createAccount(user);
            CardlessWithdrawal cw = CardlessWithdrawalFactory.createAnExpiredWithdrawal(account);

            doReturn(Optional.of(cw))
                    .when(cardlessWithdrawalRepository)
                    .findByReferenceCodeAndStatus(CardlessWithdrawalFactory.VALID_REFERENCE, WithdrawalStatus.PENDING);

            doReturn(account)
                    .when(accountService)
                    .getAccountForUpdate(cw.getAccountNumber());

            doReturn(cw)
                    .when(cardlessWithdrawalRepository)
                    .save(cw);  // Retorna o mesmo objeto (status já alterado para EXPIRED)

            // Request com reference válido (não importa o secret, pois expira antes)
            AtmCardlessRequest request = new AtmCardlessRequest(
                    CardlessWithdrawalFactory.VALID_REFERENCE,
                    CardlessWithdrawalFactory.VALID_SECRET
            );

            // ACT + ASSERT
            assertThrows(InvalidReferenceCodeException.class,
                    () -> atmService.cardlessWithdraw(request));

            // VERIFICATIONS
            verify(cardlessWithdrawalRepository).findByReferenceCodeAndStatus(
                    CardlessWithdrawalFactory.VALID_REFERENCE, WithdrawalStatus.PENDING);

            verify(accountService).getAccountForUpdate(cw.getAccountNumber());

            verify(cardlessWithdrawalRepository).save(argThat(w ->
                    w.getStatus() == WithdrawalStatus.EXPIRED));

            verify(accountService, never()).withdraw(any(), any());
            verify(transactionService, never()).createWithdrawalTransaction(any(), any(), any());

            verifyNoMoreInteractions(transactionService);
        }
    }
}