package com.github.freddy.bankApi.dto;


/**
 * DTO para devolver o token gerado após o login.
 */
public record LoginResponseDTO(String accessToken) {
}