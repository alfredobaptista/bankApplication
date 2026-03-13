package com.github.freddy.bankApi.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Resposta da api")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponseDTO<T>(
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
