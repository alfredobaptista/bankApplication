package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.dto.response.AuthTokensResponse;
import com.github.freddy.bankApi.entity.RefreshToken;
import com.github.freddy.bankApi.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenService refreshTokenService;


    //Gera acess + refresh token
    public AuthTokensResponse generateTokens(User user) {
        String accessToken = generateAccessToken(user);
        RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user);
        return new AuthTokensResponse(accessToken, refreshTokenEntity.getToken());
    }

    private String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(600); // 10 min

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("bankApi")
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(expiration)
                .claim("email", user.getEmail())
                .claim("scope", user.getRole().name())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
