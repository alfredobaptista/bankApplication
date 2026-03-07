package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.dto.response.AuthTokensResponse;
import com.github.freddy.bankApi.entity.RefreshToken;
import com.github.freddy.bankApi.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenService refreshTokenService;

    @Value("${security.jwt.acess-token.expiration}")
    private Long acessTokenExpiration;

    public AuthTokensResponse generateTokens(User user) {
        String accessToken = generateAccessToken(user);
        RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user);
        return new AuthTokensResponse(accessToken, refreshTokenEntity.getToken(), "Bearer");
    }

    private String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(acessTokenExpiration);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("bankApi")
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(expiration)
                .claim("scope", user.getRole().name())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
