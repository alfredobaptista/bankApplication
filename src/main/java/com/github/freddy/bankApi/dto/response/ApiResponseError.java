package com.github.freddy.bankApi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.freddy.bankApi.enums.ErrorCode;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponseError(
        int status,
        ErrorCode error,
        String message,
        String path,
        OffsetDateTime timestamp,
        List<FieldErrorDetail> details
) {
    public record FieldErrorDetail(String field, String message)
    { }
}


