package com.github.freddy.bankApi.enums;

public enum AccountType {
    CHECKING("Conta Corrente"),
    SAVINGS("Conta Poupança");

    private final String descricao;

    AccountType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}