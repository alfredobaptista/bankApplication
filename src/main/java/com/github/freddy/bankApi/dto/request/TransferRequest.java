package com.github.freddy.bankApi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String sourceAccountNumber,
        @NotBlank String destinationAccountNumber,
        @NotNull @Positive BigDecimal amount
) {}