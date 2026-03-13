package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.entity.RefreshToken;
import com.github.freddy.bankApi.entity.User;
import com.github.freddy.bankApi.exception.NotFoundException;
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

        var existing = refreshTokenRepository.findByUserId(user.getId());

        if(existing.isPresent() && existing.get().getExpiryDate().isBefore(expiryDate)) {
            var token = existing.get();
            token.setToken(UUID.randomUUID().toString());
            token.setExpiryDate(expiryDate);
            token.setRevoked(false);
            return refreshTokenRepository.save(token);
        }
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(expiryDate)
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(RefreshToken::isValid)
                .orElse(null);
    }

    @Transactional
    public void revokeRefreshToken(String userId) {
        RefreshToken token = refreshTokenRepository
                .findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new NotFoundException("Refresh token não encontrado"));

        if (token.isRevoked()) {
            return;
        }
        token.setRevoked(true);
    }

    @Transactional
    public int deleteExpiredTokens() {
        return refreshTokenRepository.deleteExpired(Instant.now());
    }
}