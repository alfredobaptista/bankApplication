package com.github.freddy.bankApi.dto.request;

import com.github.freddy.bankApi.enums.Role;

public record StaffUserRequest(
        String name,
        String email,
        String password,
        String biNumber,
        String phoneNumber,
        Role role  // ROLE_ADMIN, ROLE_STAFF, ROLE_SUPPORT, etc.
) {}