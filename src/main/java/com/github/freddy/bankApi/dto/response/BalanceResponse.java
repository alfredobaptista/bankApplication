package com.github.freddy.bankApi.dto.response;

import java.math.BigDecimal;

public record BalanceResponse(
        String accountNumber,
        BigDecimal ledgerBalance,
        BigDecimal availableBalance,
        String currencyCode
) {}



