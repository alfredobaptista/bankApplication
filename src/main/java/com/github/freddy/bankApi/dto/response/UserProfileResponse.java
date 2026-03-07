package com.github.freddy.bankApi.dto.response;

import com.github.freddy.bankApi.enums.Role;

import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String name,
        String identityNumber,
        String phoneNumber,
        Role role
) {}
