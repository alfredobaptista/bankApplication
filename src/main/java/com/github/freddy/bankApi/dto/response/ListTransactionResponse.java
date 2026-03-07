package com.github.freddy.bankApi.dto.response;
import java.util.List;

public record ListTransactionResponse(
        List<TransactionResponse> transactions
) {
}

