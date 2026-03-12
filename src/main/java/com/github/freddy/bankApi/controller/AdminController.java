package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.dto.request.StaffUserRequest;
import com.github.freddy.bankApi.dto.response.ApiResponse;
import com.github.freddy.bankApi.dto.response.UserProfileResponse;
import com.github.freddy.bankApi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

/**
 * Controller protegido para operações administrativas.
 * Só usuários com ROLE_ADMIN podem acessar.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")  // Protege TODO o controller
public class AdminController {

    private final UserService userService;

    /**
     * Cria um novo usuário interno (admin, staff) SEM criar conta bancária.
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserProfileResponse>> createInternalUser(
            @Valid @RequestBody StaffUserRequest dto,
            UriComponentsBuilder uriBuilder,
            HttpServletRequest request
    ) {
        var user = userService.createInternalUser(dto);
        URI location = uriBuilder
                .path("/api/v1/users/{id}")
                .buildAndExpand(user.userId())
                .toUri();
        return ResponseEntity
                .created(location)
                .body(
                        new ApiResponse<UserProfileResponse>(
                                true,
                                "Utilizador Criado com sucesso!",
                                user,
                                null,
                                request.getRequestURI(),
                                OffsetDateTime.now()
                        )
                );
    }

}