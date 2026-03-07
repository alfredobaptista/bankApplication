package com.github.freddy.bankApi.enums;

public enum WithdrawalStatus {
    PENDING,        // Criado, aguardando uso
    COMPLETED,      // Sacado com sucesso
    FAILED,         // Falhou por erro interno
    EXPIRED,        // Expirou pelo tempo
    CANCELLED,      // Cancelado pelo usuário
}
