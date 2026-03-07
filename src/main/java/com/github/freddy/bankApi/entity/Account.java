package com.github.freddy.bankApi.entity;
import com.github.freddy.bankApi.enums.AccountStatus;
import com.github.freddy.bankApi.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_number", nullable = false, updatable = false, unique = true)
    private String accountNumber;

    // Saldo contábil (ledger balance): valor oficial após todos os movimentos registrados
    @Column(name = "ledger_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal ledgerBalance;

    // Saldo disponível (available/effective balance): o que o cliente pode usar agora
    @Column(name = "available_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal availableBalance;

    @Column(name = "currency_code",  nullable = false, updatable = false)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;
}