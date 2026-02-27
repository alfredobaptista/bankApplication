package com.github.freddy.bankApi.dto.response;

import java.util.UUID;

public record UserRegistrationResponse(
        UUID clientId,
        ClientInfo client,
        AccountResponse account,
        String welcomeMessage
) {
    public record ClientInfo(
            String clientName,
            String biNumber,
            String phoneNumber,
            String email
    ) {}


    public UserRegistrationResponse(
            UUID clientId,
            ClientInfo client,
            AccountResponse account
    ) {
        this(
                clientId,
                client,
                account,
                "Bem-vindo à sua nova conta! " +
                        "Conta criada com sucesso!"  +
                        "Verifique o SMS para activar a conta e começar a usar. "
        );
    }
}



