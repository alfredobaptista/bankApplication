package com.github.freddy.bankApi.repository;

import com.github.freddy.bankApi.entity.CardlessWithdrawal;

import com.github.freddy.bankApi.enums.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardlessWithdrawalRepository extends JpaRepository<CardlessWithdrawal, Long> {

    Optional<CardlessWithdrawal> findByReferenceCodeAndStatus(String referenceCode, WithdrawalStatus status);

    Optional<CardlessWithdrawal> findByReferenceCode(String referenceCode);

    @Query("SELECT cl FROM CardlessWithdrawal cl WHERE cl.expiry < :now AND cl.status = :status")
    List<CardlessWithdrawal> findAllExpired(
            @Param("now") LocalDateTime now,
            @Param("status") WithdrawalStatus status
    );

    Page<CardlessWithdrawal> findAllByUserId(Pageable pageable, UUID userId);

}