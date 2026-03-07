package com.github.freddy.bankApi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CardlessWithdrawRequest(
        @NotNull
        @Positive(message = "Valor informado inválido")
        BigDecimal amount,
        @Size(min = 3, max = 3, message = "O código deve ter 3 digitos")
        String secretCode  // 3 dígitos escolhido pelo user
) {}
