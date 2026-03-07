package com.github.freddy.bankApi.dto.response;

import com.github.freddy.bankApi.enums.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositResponse(
        UUID transactionId,
        String accountNumber,
        String ownerName,
        BigDecimal amount,
        BigDecimal balance,
        String currencyCode,
        TransactionStatus status,
        String message
) {
}
