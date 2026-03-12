package com.github.freddy.bankApi.dto.response;

import com.github.freddy.bankApi.enums.AccountStatus;
import com.github.freddy.bankApi.enums.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        String userName,
        String accountNumber,
        BigDecimal availableBalance,
        BigDecimal leadgerBalance,
        String currencyCode,
        AccountType accountType,
        AccountStatus accountStatus
) { }
