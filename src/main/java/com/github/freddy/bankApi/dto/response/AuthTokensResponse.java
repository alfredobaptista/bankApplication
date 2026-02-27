package com.github.freddy.bankApi.dto.response;


// Record simples para retornar ambos
public record AuthTokensResponse(String accessToken, String refreshToken) {}
