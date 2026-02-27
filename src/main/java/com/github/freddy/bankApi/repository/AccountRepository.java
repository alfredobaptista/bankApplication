package com.github.freddy.bankApi.repository;

import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.enums.AccountStatus;
import com.github.freddy.bankApi.enums.AccountType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    // Método crucial para Transferências e Saques:
    // O LockModeType.PESSIMISTIC_WRITE impede que outra transação leia ou escreva
    // nesta conta enquanto você estiver processando o saldo.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);

    // Query nativa para buscar o próximo valor da SEQUENCE
    @Query(value = "SELECT nextval('seq_account_number')", nativeQuery = true)
    Long getNextSequenceValue();

    List<Account> findByAccountTypeAndStatus(AccountType accountType, AccountStatus accountStatus);

    boolean existsByAccountNumber(String accountNumber);
}
