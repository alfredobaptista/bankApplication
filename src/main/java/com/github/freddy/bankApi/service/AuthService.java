package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.dto.request.LoginRequest;
import com.github.freddy.bankApi.dto.request.LogoutRequest;
import com.github.freddy.bankApi.dto.request.RefreshTokenRequest;
import com.github.freddy.bankApi.dto.request.RegisterRequest;
import com.github.freddy.bankApi.dto.response.AuthTokensResponse;
import com.github.freddy.bankApi.entity.RefreshToken;
import com.github.freddy.bankApi.entity.User;
import com.github.freddy.bankApi.exception.BusinessLogicException;
import com.github.freddy.bankApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.freddy.bankApi.dto.response.RegistrationResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serviço central de autenticação: login, registro e refresh token.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    /**
     * Autentica o usuário e retorna access token + refresh token.
     */
    public AuthTokensResponse authenticate(LoginRequest login) {
        log.debug("Tentativa de login com email: {}", login.email());

        User user = userRepository.findByEmail(login.email())
                .orElseThrow(() -> {
                    log.warn("Login falhou: email não encontrado - {}", login.email());
                    return new BadCredentialsException("Credenciais inválidas");
                });
        if (!passwordEncoder.matches(login.password(), user.getPassword())) {
            log.warn("Login falhou: senha incorreta para email {}", login.email());
            throw new BadCredentialsException("Credenciais inválidas");
        }

        log.info("Login realizado com sucesso: usuário={} (ID: {})", user.getName(), user.getId());

        return tokenService.generateTokens(user);
    }

    /**
     * Registra novo usuário e cria conta padrão.
     * Delega a lógica pesada para UserService.
     */
    public RegistrationResponse register(RegisterRequest data) {
        log.debug("Tentativa de registro: email={}, BI={}", data.email(), data.biNumber());

        if (userRepository.existsByEmail(data.email())) {
            log.warn("Registro falhou: email já cadastrado - {}", data.email());
            throw new BusinessLogicException("Este email já está cadastrado");
        }

        String biUpper = data.biNumber().toUpperCase();
        if (userRepository.existsByBi(biUpper)) {
            log.warn("Registro falhou: BI já cadastrado - {}", biUpper);
            throw new BusinessLogicException("Este BI já está cadastrado");
        }

        RegistrationResponse response = userService.createNewUser(data);

        log.info("Registro concluído: clientId={}, email={}, conta={}",
                response.clientId(), data.email(), response.account().accountNumber());

        return response;
    }

    /**
     * Gera novos tokens usando um refresh token válido.
     * Implementa rotação: revoga o refresh antigo.
     */
    public AuthTokensResponse refresh(RefreshTokenRequest request) {
        log.debug("Tentativa de refresh com token: {}", request.refreshToken().substring(0, 10) + "...");

        RefreshToken tokenEntity = refreshTokenService.findByToken(request.refreshToken());
        if (tokenEntity == null || !tokenEntity.isValid()) {
            log.warn("Refresh falhou: token inválido ou expirado");
            throw new BadCredentialsException("Refresh token inválido ou expirado");
        }

        // Rotação: revoga o token usado
        refreshTokenService.revokeRefreshToken(tokenEntity.getUser().getId().toString());
        log.debug("Refresh token antigo revogado para usuário ID: {}", tokenEntity.getUser().getId());

        // Gera novos tokens
        AuthTokensResponse newTokens = tokenService.generateTokens(tokenEntity.getUser());

        log.info("Refresh realizado com sucesso: novo access token gerado para usuário ID: {}",
                tokenEntity.getUser().getId());
        return newTokens;
    }

    public  void logout(String userId) {
        refreshTokenService.revokeRefreshToken(userId);
    }
}