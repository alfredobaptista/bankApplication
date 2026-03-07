package com.github.freddy.bankApi.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthTokensResponse(

        @JsonProperty("acess_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType
        ) {}
