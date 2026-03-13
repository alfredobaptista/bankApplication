package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.dto.request.AtmCardlessRequest;
import com.github.freddy.bankApi.dto.response.AtmCardlessResponse;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.CardlessWithdrawal;
import com.github.freddy.bankApi.entity.Transaction;
import com.github.freddy.bankApi.entity.User;
import com.github.freddy.bankApi.enums.AccountType;
import com.github.freddy.bankApi.enums.TransactionStatus;
import com.github.freddy.bankApi.enums.TransactionType;
import com.github.freddy.bankApi.enums.WithdrawalStatus;
import com.github.freddy.bankApi.exception.InsufficientBalanceException;
import com.github.freddy.bankApi.exception.InvalidReferenceCodeException;
import com.github.freddy.bankApi.exception.NotFoundException;
import com.github.freddy.bankApi.repository.CardlessWithdrawalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtmService → Cardless Withdrawal Flow")
class AtmServiceTest {

    @Mock
    private CardlessWithdrawalRepository cardlessRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AtmService atmService;



    private static class TestDataFactory {

        static final String VALID_REFERENCE = "82452872";
        static final String VALID_SECRET    = "123";
        static final String INVALID_SECRET  = "000000";
        static final BigDecimal AMOUNT      = new BigDecimal("7500.00");
        static final BigDecimal INITIAL_BAL = new BigDecimal("20000.00");

        static User aUser() {
            return User.builder()
                    .id(UUID.randomUUID())
                    .name("Testino da Silva")
                    .email("testino@example.ao")
                    .bi("123456789LA012")
                    .phoneNumber("+244923456789")
                    .build();
        }

        static Account anAccount(User user) {
            return Account.builder()
                    .id(UUID.randomUUID())
                    .accountNumber("AO006.0000.98765432109")
                    .accountType(AccountType.CHECKING)
                    .currencyCode("AOA")
                    .availableBalance(INITIAL_BAL)
                    .ledgerBalance(INITIAL_BAL)
                    .user(user)
                    .version(1L)
                    .build();
        }

        static CardlessWithdrawal aPendingWithdrawal(Account account) {
            return CardlessWithdrawal.builder()
                    .id(100L)
                    .userId(account.getUser().getId())
                    .accountNumber(account.getAccountNumber())
                    .referenceCode(VALID_REFERENCE)
                    .secretCode(VALID_SECRET)
                    .amount(AMOUNT)
                    .status(WithdrawalStatus.PENDING)
                    .expiry(LocalDateTime.now().plusMinutes(20))
                    .build();
        }

        static CardlessWithdrawal anExpiredWithdrawal(Account account) {
            CardlessWithdrawal cw = aPendingWithdrawal(account);
            cw.setExpiry(LocalDateTime.now().minusMinutes(10));
            return cw;
        }

        static AtmCardlessRequest validRequest() {
            return new AtmCardlessRequest(VALID_REFERENCE, VALID_SECRET);
        }

        static AtmCardlessRequest requestWithWrongSecret() {
            return new AtmCardlessRequest(VALID_REFERENCE, INVALID_SECRET);
        }

        static AtmCardlessRequest requestWithInvalidRef() {
            return new AtmCardlessRequest("INVALID-XXX", VALID_SECRET);
        }

        static Transaction aCompletedTransaction(Account account, BigDecimal amount, String refCode) {
            return Transaction.builder()
                    .id(UUID.randomUUID())
                    .type(TransactionType.WITHDRAWAL)
                    .amount(amount)
                    .sourceAccount(account)
                    .status(TransactionStatus.COMPLETED)
                    .build();
        }
    }

    private Account account;
    private CardlessWithdrawal withdrawal;

    @BeforeEach
    void setupCommonTestData() {
        User user = TestDataFactory.aUser();
        account = TestDataFactory.anAccount(user);
        withdrawal = TestDataFactory.aPendingWithdrawal(account);
    }

    @Nested
    @DisplayName("Fluxo principal (Happy Path)")
    class HappyPath {

        @Test
        @DisplayName("Deve concluir levantamento cardless com sucesso")
        void shouldSuccessfullyCompleteCardlessWithdrawal() {
            // Arrange
            AtmCardlessRequest request = TestDataFactory.validRequest();
            Transaction transaction = TestDataFactory.aCompletedTransaction(account, TestDataFactory.AMOUNT, TestDataFactory.VALID_REFERENCE);

            when(cardlessRepository.findByReferenceCodeAndStatus(
                    TestDataFactory.VALID_REFERENCE, WithdrawalStatus.PENDING))
                    .thenReturn(Optional.of(withdrawal));

            when(accountService.getAccountForUpdate(account.getAccountNumber()))
                    .thenReturn(account);

            when(transactionService.createWithdrawalTransaction(
                    eq(account), eq(TestDataFactory.AMOUNT), eq(TestDataFactory.VALID_REFERENCE)))
                    .thenReturn(transaction);

            // Act
            AtmCardlessResponse response = atmService.cardlessWithdrawAtAtm(request);

            // Assert
            assertThat(response.transactionId()).isEqualTo(transaction.getId().toString());
            assertThat(response.amountWithdrawn()).isEqualByComparingTo(TestDataFactory.AMOUNT);

            assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.COMPLETED);

            // Verifications
            verify(cardlessRepository).findByReferenceCodeAndStatus(anyString(), eq(WithdrawalStatus.PENDING));
            verify(accountService).getAccountForUpdate(account.getAccountNumber());
            verify(accountService).withdraw(account, TestDataFactory.AMOUNT);
            verify(transactionService).createWithdrawalTransaction(account, TestDataFactory.AMOUNT, TestDataFactory.VALID_REFERENCE);
            verify(cardlessRepository).save(withdrawal);
            verifyNoMoreInteractions(cardlessRepository, accountService, transactionService);
        }
    }

    @Nested
    @DisplayName("Casos de falha e validações")
    class FailureCases {

        @Test
        @DisplayName("Deve lançar NotFoundException quando código de referência não existe")
        void shouldThrowNotFoundWhenReferenceCodeDoesNotExist() {
            String invalidRef = "INVALID-REF-999";

            when(cardlessRepository.findByReferenceCodeAndStatus(
                    eq(invalidRef),
                    eq(WithdrawalStatus.PENDING)))
                    .thenReturn(Optional.empty());

            AtmCardlessRequest request = new AtmCardlessRequest(invalidRef, TestDataFactory.VALID_SECRET);

            assertThatThrownBy(() -> atmService.cardlessWithdrawAtAtm(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Código de referência inválido");

            verify(cardlessRepository).findByReferenceCodeAndStatus(invalidRef, WithdrawalStatus.PENDING);
            verifyNoMoreInteractions(cardlessRepository);
            verifyNoInteractions(accountService, transactionService);
        }

        @Test
        @DisplayName("Deve marcar como expirado e reverter saldo quando código expirou")
        void shouldExpireAndRefundWhenCodeIsExpired() {
            CardlessWithdrawal expired = TestDataFactory.anExpiredWithdrawal(account);

            when(cardlessRepository.findByReferenceCodeAndStatus(
                    TestDataFactory.VALID_REFERENCE, WithdrawalStatus.PENDING))
                    .thenReturn(Optional.of(expired));

            when(accountService.getAccountForUpdate(anyString())).thenReturn(account);

            assertThatThrownBy(() -> atmService.cardlessWithdrawAtAtm(TestDataFactory.validRequest()))
                    .isInstanceOf(InvalidReferenceCodeException.class)
                    .hasMessage("Levantamento expirado");

            assertThat(expired.getStatus()).isEqualTo(WithdrawalStatus.EXPIRED);
            assertThat(account.getAvailableBalance())
                    .isEqualByComparingTo(TestDataFactory.INITIAL_BAL.add(TestDataFactory.AMOUNT));

            verify(cardlessRepository).save(expired);
            verifyNoInteractions(transactionService);
        }

        @Test
        @DisplayName("Deve rejeitar código secreto incorreto sem alterar estado nem persistir")
        void shouldRejectIncorrectSecretCode() {
            // Arrange
            String validRef = TestDataFactory.VALID_REFERENCE;
            AtmCardlessRequest wrongSecretRequest = TestDataFactory.requestWithWrongSecret();

            // Configuração do mock com argumentos exatos (mais seguro)
            when(cardlessRepository.findByReferenceCodeAndStatus(
                    eq(validRef),
                    eq(WithdrawalStatus.PENDING)))
                    .thenReturn(Optional.of(withdrawal));

            // Act + Assert exceção
            assertThatThrownBy(() -> atmService.cardlessWithdrawAtAtm(wrongSecretRequest))
                    .isInstanceOf(InvalidReferenceCodeException.class);   // ← se falhar aqui, a mensagem está diferente!

            // Verificações de segurança (o que realmente importa nesse fluxo)
            assertThat(withdrawal.getStatus())          // deve continuar PENDING
                    .isEqualTo(WithdrawalStatus.PENDING);

            verify(cardlessRepository, times(1))
                    .findByReferenceCodeAndStatus(validRef, WithdrawalStatus.PENDING);

            // Crucial: NÃO deve ter salvo nada
            verify(cardlessRepository, never()).save(any(CardlessWithdrawal.class));

            // Nenhuma interação com os outros serviços
            verifyNoInteractions(accountService);
            verifyNoInteractions(transactionService);
        }

        @Test
        @DisplayName("Deve propagar exceção de saldo insuficiente")
        void shouldPropagateInsufficientBalance() {
            when(cardlessRepository.findByReferenceCodeAndStatus(
                    TestDataFactory.VALID_REFERENCE, WithdrawalStatus.PENDING))
                    .thenReturn(Optional.of(withdrawal));

            when(accountService.getAccountForUpdate(anyString())).thenReturn(account);

            doThrow(new InsufficientBalanceException("Saldo insuficiente para levantamento"))
                    .when(accountService).withdraw(any(), any());

            assertThatThrownBy(() -> atmService.cardlessWithdrawAtAtm(TestDataFactory.validRequest()))
                    .isInstanceOf(InsufficientBalanceException.class);

            assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.PENDING);
        }
    }
}