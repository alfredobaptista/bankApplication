package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.entity.RefreshToken;
import com.github.freddy.bankApi.entity.User;
import com.github.freddy.bankApi.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration-days:7}")
    private long refreshExpirationDays;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        Instant expiryDate = Instant.now().plusSeconds(refreshExpirationDays * 86_400);

        return refreshTokenRepository.findByUser(user)
                .map(existing -> {
                    existing.setToken(UUID.randomUUID().toString());
                    existing.setExpiryDate(expiryDate);
                    existing.setRevoked(false);
                    return refreshTokenRepository.save(existing);
                })
                .orElseGet(() -> {
                    RefreshToken refreshToken = RefreshToken.builder()
                            .user(user)
                            .token(UUID.randomUUID().toString())
                            .expiryDate(expiryDate)
                            .revoked(false)
                            .build();
                    return refreshTokenRepository.save(refreshToken);
                });
    }


    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(RefreshToken::isValid)
                .orElse(null);
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}