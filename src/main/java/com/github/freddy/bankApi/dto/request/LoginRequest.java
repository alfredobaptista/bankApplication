package com.github.freddy.bankApi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "E-mail obrigatorio")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Password obrigatorio")
        @Size(min = 8, message = "Senha inválida")
        String password
) {
}
