package com.github.freddy.bankApi.enums;

import lombok.Getter;

@Getter
public enum Role {
    ROLE_CUSTOMER("Cliente"),
    ROLE_STAFF("Funcionário"),
    ROLE_ADMIN("Administrador");

    private final String descricao;

    Role(String descricao) {
        this.descricao = descricao;
    }
}