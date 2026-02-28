package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.dto.request.LoginRequest;
import com.github.freddy.bankApi.dto.request.RefreshTokenRequest;
import com.github.freddy.bankApi.dto.request.UserRegistrationRequest;
import com.github.freddy.bankApi.dto.response.AuthTokensResponse;
import com.github.freddy.bankApi.dto.response.UserRegistrationResponse;
import com.github.freddy.bankApi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller responsável por autenticação e gerenciamento de sessão.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * Registra um novo usuário e cria conta padrão.
     * Retorna 201 Created com Location header apontando para o perfil do cliente.
     */
    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(
            @RequestBody @Valid UserRegistrationRequest data,
            UriComponentsBuilder uriBuilder
    ) {
        log.debug("Requisição de registro recebida: email={}", data.email());

        UserRegistrationResponse response = authService.register(data);
        URI location = uriBuilder
                .path("/api/v1/clients/{id}")
                .buildAndExpand(response.clientId())
                .toUri();

        log.info("Registro concluído com sucesso: clientId={}, email={}",
                response.clientId(), data.email());

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Autentica o usuário e retorna access token + refresh token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(
            @Valid @RequestBody LoginRequest data
    ) {
        log.debug("Requisição de login: email={}", data.email());

        AuthTokensResponse tokens = authService.authenticate(data);

        log.info("Login bem-sucedido: email={}", data.email());

        return ResponseEntity.ok(tokens);
    }

    /**
     * Gera novos access + refresh tokens usando um refresh token válido.
     * Implementa rotação automática.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthTokensResponse> refreshToken(
            @RequestBody @Valid RefreshTokenRequest data
    ) {
        log.debug("Requisição de refresh token");

        AuthTokensResponse newTokens = authService.refresh(data);

        log.info("Refresh realizado com sucesso");

        return ResponseEntity.ok(newTokens);
    }

    /**
     * Realiza logout: revoga todos os refresh tokens do usuário logado.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.info("Requisição de logout para usuário ID: {}", userId);

        authService.logout(userId);

        log.info("Logout concluído com sucesso para usuário ID: {}", userId);

        return ResponseEntity.noContent().build();
    }
}