package com.github.freddy.bankApi.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,  // "Operação realizada com sucesso",
        T data,
        PaginationMeta paginationMeta,
        String path,
        OffsetDateTime timestamp
) {
    public record PaginationMeta(
            int page,
            int perPage,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious
    ) {}
}
