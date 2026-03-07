package com.github.freddy.bankApi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtmCompleteCardlessRequest(
        @NotBlank
        String referenceCode,
        @Size(min = 3, max = 3)
        String secretCode
) {
}
