package com.github.freddy.bankApi.entity;

import com.github.freddy.bankApi.enums.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_cardless_with_drawal")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CardlessWithdrawal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    private BigDecimal amount;

    @Column(name = "reference_code", nullable = false, unique = true, updatable = false)
    private String referenceCode;

    @Column(name = "secret_code", nullable = false, updatable = false)
    private String secretCode;

    @Column(nullable = false, updatable = false)
    private LocalDateTime expiry;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WithdrawalStatus status;
}