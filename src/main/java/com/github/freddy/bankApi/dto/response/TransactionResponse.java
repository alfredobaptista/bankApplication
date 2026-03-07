package com.github.freddy.bankApi.dto.response;

import com.github.freddy.bankApi.enums.TransactionStatus;
import com.github.freddy.bankApi.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String type,
        BigDecimal amount,
        String description,
        String status,
        LocalDateTime createdAt

) {
}
