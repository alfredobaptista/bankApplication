package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.dto.request.LoginRequest;
import com.github.freddy.bankApi.dto.request.LogoutRequest;
import com.github.freddy.bankApi.dto.request.RefreshTokenRequest;
import com.github.freddy.bankApi.dto.request.RegisterRequest;
import com.github.freddy.bankApi.dto.response.ApiResponse;
import com.github.freddy.bankApi.dto.response.AuthTokensResponse;
import com.github.freddy.bankApi.dto.response.RegistrationResponse;
import com.github.freddy.bankApi.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(
            @RequestBody @Valid RegisterRequest data,
            HttpServletRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        log.debug("Requisição de registro recebida: email={}", data.email());

        RegistrationResponse response = authService.register(data);
        URI location = uriBuilder
                .path("/api/v1/clients/{id}")
                .buildAndExpand(response.clientId())
                .toUri();

        log.info("Registro concluído com sucesso: clientId={}, email={}",
                response.clientId(),
                data.email()
        );
        return ResponseEntity
                .created(location)
                .body(
                        new ApiResponse<RegistrationResponse>(
                              true,
                              "Registro Efectuado com sucesso!",
                                response,
                                null,
                                request.getRequestURI(),
                                OffsetDateTime.now()
                        )
                );
    }

    /**
     * Autentica o usuário e retorna access token + refresh token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokensResponse>> login(
            @Valid @RequestBody LoginRequest data,
            HttpServletRequest request
    ) {
        log.debug("Requisição de login: email={}", data.email());

        AuthTokensResponse tokens = authService.authenticate(data);

        log.info("Login bem-sucedido: email={}", data.email());

        return ResponseEntity
                .ok(
                        new ApiResponse<AuthTokensResponse>(
                                true,
                                "Login Efectuado com sucesso",
                                tokens,
                                null,
                                request.getRequestURI(),
                                OffsetDateTime.now()
                        )
                );
    }

    /**
     * Gera novos access + refresh tokens usando um refresh token válido.
     * Implementa rotação automática.
     */

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokensResponse>> refreshToken(
            @RequestBody @Valid RefreshTokenRequest data,
            HttpServletRequest request
    ) {
        log.debug("Requisição de refresh token");

        AuthTokensResponse newTokens = authService.refresh(data);

        log.info("Refresh realizado com sucesso");

        return ResponseEntity.ok(
               new ApiResponse<AuthTokensResponse>(
                       true,
                       "Token emitido com sucesso",
                       newTokens,
                       null,
                       request.getRequestURI(),
                       OffsetDateTime.now()
               )
        );
    }

    /**
     * Realiza logout: revoga todos os refresh tokens do usuário logado.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid LogoutRequest data
    ) {
        var userId = jwt.getSubject();
        log.info("Requisição de logout para usuário ID: {}", userId);

        authService.logout(userId, data);

        log.info("Logout concluído com sucesso para usuário ID: {}", userId);

        return ResponseEntity.noContent().build();
    }
}