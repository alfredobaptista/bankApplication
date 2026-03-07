package com.github.freddy.bankApi.enums;

public enum TransactionStatus {
    PENDING,          // Padrão inicial: aguardando processamento (ex: código gerado, mas ainda não usado no ATM)
    AUTHORIZED,       // Autorizado (ex: código validado, fundos reservados)
    COMPLETED,        // Concluído com sucesso (ex: dinheiro retirado, saldo atualizado)
    FAILED,           // Falhou (ex: código errado, saldo insuficiente no momento do uso)
    EXPIRED,          // Expirou (ex: código passado os 30 minutos)
    CANCELLED,        // Cancelado pelo usuário ou sistema antes de completar
    REVERSED,         // Revertido (ex: devolução por erro ou fraude detectada depois)
}