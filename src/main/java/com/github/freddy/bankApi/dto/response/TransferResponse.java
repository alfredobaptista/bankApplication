package com.github.freddy.bankApi.dto.response;

import com.github.freddy.bankApi.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
public record TransferResponse(
        String transactionId,
        String destinationAccount,
        String destinationOwnerName,
        BigDecimal amount,
        BigDecimal balance,
        String currencyCode,
        TransactionStatus status,
        String message,
        LocalDateTime timestamp
) {
}
