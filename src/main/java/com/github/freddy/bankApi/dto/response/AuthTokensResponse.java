package com.github.freddy.bankApi.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta contendo token JWT")
public record AuthTokensResponse(

        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
        @JsonProperty("acess_token")
        String accessToken,

        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
        @JsonProperty("refresh_token")
        String refreshToken,

        @Schema(example = "Bearer")
        @JsonProperty("token_type")
        String tokenType
        ) {}
