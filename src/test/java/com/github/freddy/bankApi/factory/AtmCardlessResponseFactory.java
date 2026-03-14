package com.github.freddy.bankApi.factory;

import com.github.freddy.bankApi.dto.response.AtmCardlessResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AtmCardlessResponseFactory {
    public static LocalDateTime now = LocalDateTime.now();
    public static AtmCardlessResponse createCardlessResponse() {
        return new AtmCardlessResponse("12345", BigDecimal.valueOf(2000.00), now);
    }
}
