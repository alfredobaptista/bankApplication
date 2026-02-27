package com.github.freddy.bankApi.enums;
import lombok.Getter;

@Getter
public enum TransactionType {
    DEPOSIT("Depósito"),
    WITHDRAWAL("Levantamento"),
    TRANSFER("Transferência"),
    FEE("Taxa Bancária"),       // Sugestão: para cobranças automáticas
    INTEREST("Juros");          // Sugestão: para rendimentos de poupança

    private final String descricao;

    TransactionType(String descricao) {
        this.descricao = descricao;
    }
}