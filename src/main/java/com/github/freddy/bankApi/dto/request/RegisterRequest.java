package com.github.freddy.bankApi.dto.request;

import jakarta.validation.constraints.*;
import com.github.freddy.bankApi.enums.AccountType;

public record RegisterRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 5, max = 100)
        String clientName,

        @NotBlank(message = "O BI é obrigatório")
        @Size(min = 14, max = 14, message = "O BI deve ter exatamente 14 caracteres")
        @Pattern(
                regexp = "^[0-9]{9}[A-Z]{2}[0-9]{3}$",
                message = "O BI deve seguir o formato padrão angolano (9 dígitos + 2 letras + 3 dígitos)"
        )
        String biNumber,


        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail com formato inválido")
        String email,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern( regexp = "^(?:\\+244)?\\d{9}$", message = "O telefone deve ter 9 dígitos, com prefixo +244 opcional" )
        String phoneNumber,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 128, message = "A senha deve ter no mínimo 8 caracteres")
        String password,

        @NotNull(message = "O tipo de conta é obrigatório")
        AccountType accountType
) {}

