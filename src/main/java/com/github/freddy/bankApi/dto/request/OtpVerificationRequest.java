package com.github.freddy.bankApi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// request
public record OtpVerificationRequest(
        @Pattern(
                regexp = "^(?:\\+244)?\\d{9}$",
                message = "O telefone deve ter 9 dígitos, com prefixo +244 opcional"
        )
        String phoneNumber,

        @Pattern(regexp =  "", message = "")
        String otpCode
) {}


