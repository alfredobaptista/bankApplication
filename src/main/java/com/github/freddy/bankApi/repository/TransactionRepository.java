package com.github.freddy.bankApi.repository;

import com.github.freddy.bankApi.dto.SuspiciousAccount;
import com.github.freddy.bankApi.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.accountNumber = :accountNumber")
    Page<Transaction> findByAccountNumber(
            @Param("accountNumber") String accountNumber,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.user.id = :userId")
    Page<Transaction> findByUserId(@Param("userId") UUID userId, Pageable pageable);


    Page<Transaction> findBySourceAccountUserId(UUID userId, Pageable pageable);

    @Query(value = """
        SELECT a.account_number AS accountNumber, COUNT(*) AS transactionCount
        FROM tb_transactions t
        JOIN tb_accounts a ON t.source_account_id = a.id
        WHERE t.created_at >= :since
          AND t.amount >= :threshold
        GROUP BY a.account_number
        HAVING COUNT(*) >= :count
    """,
            nativeQuery = true
    )
    List<SuspiciousAccount> findSuspiciousAccounts(
            @Param("since") LocalDateTime since,
            @Param("threshold") BigDecimal threshold,
            @Param("count") Long count
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.sourceAccount.user.id = :userId " +
            "AND t.createdAt >= :start AND t.createdAt < :end " +
            "AND t.status = 'COMPLETED'")
    BigDecimal sumTransactionsByUserAndDate(
            @Param("userId") UUID userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
