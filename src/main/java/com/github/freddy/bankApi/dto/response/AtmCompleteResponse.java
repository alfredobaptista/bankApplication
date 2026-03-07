package com.github.freddy.bankApi.dto.response;

import java.math.BigDecimal;

public record AtmCompleteResponse(
        String transactionId,     // ID da transação criada
        BigDecimal amountWithdrawn  // Valor efetivamente levantado
) {
}
