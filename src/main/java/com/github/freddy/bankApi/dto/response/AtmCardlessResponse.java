package com.github.freddy.bankApi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AtmCardlessResponse(
        String transactionId,     // ID da transação criada
        BigDecimal amountWithdrawn,// Valor efetivamente levantado
        LocalDateTime timestamp
) {
}
