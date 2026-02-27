package com.github.freddy.bankApi.enums;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    PENDING("Aguardando processamento"),
    COMPLETED("Concluída com sucesso"),
    FAILED("Falhou"),
    REVERSED("Estornada/Devolvida");

    private final String descricao;

    TransactionStatus(String descricao) {
        this.descricao = descricao;
    }
}