package com.github.freddy.bankApi.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
public record TransferRequest(
        @NotBlank(message = "Informe o numero de conta")
        @Pattern(
                regexp = "^AO[0-9]{7}$",
                message = "Número de conta inválido"
        )
        String accountNumber,

        @NotNull(message = "Deve informar a quantia")
        @Positive(message = "Valor inváliod")
        BigDecimal amount
) {}