package com.github.freddy.bankApi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DepositRequest(

        @NotBlank(message = "Informe o número de conta")
        @Pattern(
                regexp = "^AO[0-9]{7}$",
                message = "Núumero de conta inválido"
        )
        String accountNumber,

        String biNumber,

        @NotNull(message = "Informe um valor")
        @Positive(message = "Valor invalido")
        BigDecimal amount
) {}
