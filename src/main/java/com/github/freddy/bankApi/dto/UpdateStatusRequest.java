package com.github.freddy.bankApi.dto;

import com.github.freddy.bankApi.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "O status da conta é obrigatório")
        AccountStatus status
) {}
