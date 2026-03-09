package com.github.freddy.bankApi.mapper;

import com.github.freddy.bankApi.dto.request.StaffUserRequest;
import com.github.freddy.bankApi.dto.request.RegisterRequest;
import com.github.freddy.bankApi.dto.response.AccountResponse;
import com.github.freddy.bankApi.dto.response.RegistrationResponse;
import com.github.freddy.bankApi.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper para conversão entre DTOs e entidade User.
 */
@Mapper(componentModel = "spring", imports = {RoundingMode.class, BigDecimal.class})
public abstract class UserMapper {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "name", expression = "java(request.clientName())")
    @Mapping(target = "email", expression = "java(request.email())")
    @Mapping(target = "bi", expression = "java(request.biNumber().toUpperCase())")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "normalizePhone")
    @Mapping(target = "password", expression = "java(passwordEncoder.encode(request.password()))")
    @Mapping(target = "role", constant = "ROLE_CUSTOMER")

    public abstract User toEntity(RegisterRequest request);

    @Mapping(target = "email", expression = "java(request.email())")
    @Mapping(target = "bi", expression = "java(request.biNumber().toUpperCase())")
    @Mapping(target = "phoneNumber", expression = "java(normalizePhoneNumber(request.phoneNumber()))")
    @Mapping(target = "password", expression = "java(passwordEncoder.encode(request.password()))")
    @Mapping(target = "role", expression = "java(request.role())")
    public abstract User toInternalEntity(StaffUserRequest request);


    @Mapping(target = "clientId", expression = "java(user.getId())")
    @Mapping(target = "client", expression = "java(toClientInfo(user))")
    @Mapping(target = "account", source = "account")
    @Mapping(target = "welcomeMessage", constant = "Bem-vindo à sua nova conta! Conta criada com sucesso! Verifique o SMS para activar a conta e começar a usar.")
    public abstract RegistrationResponse toResponse(User user, AccountResponse account);

    // Métodos default (não precisam de implementação gerada)
    protected RegistrationResponse.ClientInfo toClientInfo(User user) {
        return new RegistrationResponse.ClientInfo(
                user.getName(),
                user.getBi(),
                user.getPhoneNumber(),
                user.getEmail()
        );
    }

    @Named("normalizePhone")
    public  String normalizePhoneNumber(String phoneNumber) {
        String digits = phoneNumber.replaceAll("\\D", "");

        if (digits.length() == 9) {
            return "+244" + digits;
        }
        if (digits.length() == 12 && digits.startsWith("244")) {
            return "+" + digits;
        }
        if (digits.length() == 13 && digits.startsWith("+244")) {
            return digits;
        }

        throw new IllegalArgumentException("Formato de telefone inválido: " + phoneNumber +
                ". Esperado: 9 dígitos ou +244 seguido de 9 dígitos");
    }
}