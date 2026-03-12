package com.github.freddy.bankApi.dto.response;

import com.github.freddy.bankApi.enums.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CardlessWithdrawalDetailsResponse(
        String accountNumber,
        BigDecimal amount,
         String referenceCode,
         LocalDateTime expiry,
         WithdrawalStatus status,
         LocalDateTime createdAt
) {
}
