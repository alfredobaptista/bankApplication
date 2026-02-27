package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.dto.request.InternalUserRequest;
import com.github.freddy.bankApi.dto.response.UserProfileResponse;
import com.github.freddy.bankApi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

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
    public ResponseEntity<UserProfileResponse> createInternalUser(
            @Valid @RequestBody InternalUserRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var user = userService.createInternalUser(request);
        URI location = uriBuilder
                .path("/api/v1/users/{id}")
                .buildAndExpand(user.userId())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    // Outros endpoints admin futuros (ex: bloquear conta, ver logs, etc.)
    // @GetMapping("/accounts/{accountNumber}/audit")
    // public ResponseEntity<AuditLog> getAccountAudit(@PathVariable String accountNumber) { ... }
}