package com.github.freddy.bankApi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de login")
public record LoginRequest(

        @Schema(example = "user@email.com")
        @NotBlank(message = "E-mail obrigatorio")
        @Email(message = "E-mail inválido")
        String email,

        @Schema(example = "123!")
        @NotBlank(message = "Password obrigatorio")
        @Size(min = 8, message = "Senha inválida")
        String password
) {
}
