package com.github.freddy.bankApi.factory;

import com.github.freddy.bankApi.entity.User;

import java.util.UUID;

public class UserFactory {
    public static User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .name("Testino da Silva")
                .email("testino@example.ao")
                .bi("123456789LA012")
                .phoneNumber("+244923456789")
                .build();
    }
}
