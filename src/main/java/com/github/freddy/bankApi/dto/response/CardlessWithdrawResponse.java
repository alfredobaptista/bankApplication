package com.github.freddy.bankApi.dto.response;

import java.math.BigDecimal;

// Response com o código para usar no ATM
public record CardlessWithdrawResponse(
        String referenceCode,
        BigDecimal amount
) {}
