package com.github.freddy.bankApi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "E-mail obrigatorio")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Password obrigatorio")
        String password
) {
}
