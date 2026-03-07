package com.github.freddy.bankApi.dto.response;

import java.util.UUID;

public record RegistrationResponse(
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


    public RegistrationResponse(
            UUID clientId,
            ClientInfo client,
            AccountResponse account
    ) {
        this(
                clientId,
                client,
                account,
                "Bem-vindo à sua nova conta! " +
                        "Conta criada com sucesso!"
        );
    }
}



