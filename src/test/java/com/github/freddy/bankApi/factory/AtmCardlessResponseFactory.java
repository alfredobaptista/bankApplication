package com.github.freddy.bankApi.factory;

import com.github.freddy.bankApi.dto.response.ApiResponseDTO;
import com.github.freddy.bankApi.dto.response.AtmCardlessResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class ApiResponseDTOFactory {

    public static ApiResponseDTO<AtmCardlessResponse> createCardlessResponse() {
        return new ApiResponseDTO<>(
                true,
                "Levantamento realizado",
                new AtmCardlessResponse("12345", BigDecimal.valueOf(2000.00), LocalDateTime.now()),
                null,
                null,
                OffsetDateTime.now()
        );
    }
}
