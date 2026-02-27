package com.github.freddy.bankApi.enums;

import lombok.Getter;

@Getter
public enum AccountStatus {
    ACTIVE("Ativa"),
    BLOCKED("Bloqueada"),
    CLOSED("Encerrada"),
    PENDING_ACTIVATION("Pendente");

    private final String descricao;

    AccountStatus(String descricao) {
        this.descricao = descricao;
    }
}