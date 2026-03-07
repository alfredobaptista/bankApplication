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


    /*
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserProfileResponse>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,          // filtro por ROLE_CLIENT, ROLE_STAFF, etc.
            @RequestParam(required = false) String bi,            // filtro por BI
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserProfileResponse> users = userService.listUsers(pageable, role, bi);

        return ResponseEntity.ok(new ApiResponse<>(
                true, "Lista de utilizadores obtida", users.getContent(),
                new ApiResponse.PaginationMeta(/* ... */
                /*request.getRequestURI(),
                OffsetDateTime.now()
        ));
    }

    */

    /*

    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest dto
    ) {
        var updated = userService.updateUser(userId, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Utilizador atualizado", updated, null, ...));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> changeUserStatus(
            @PathVariable UUID userId,
            @RequestBody @Valid UserStatusRequest request   // { "active": false, "reason": "Suspeita de fraude" }
    ) {
        userService.changeUserStatus(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Estado alterado com sucesso", null, ...));
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountDetails(@PathVariable String accountNumber) {
        var account = accountService.getAccountDetails(accountNumber);
        return ResponseEntity.ok(new ApiResponse<>(true, "Detalhes da conta", account, ...));
    }

    @PatchMapping("/accounts/{accountNumber}/status")
    public ResponseEntity<ApiResponse<Void>> changeAccountStatus(
            @PathVariable String accountNumber,
            @RequestBody AccountStatusRequest request  // { "status": "BLOCKED", "reason": "..." }
    ) { ... }

    */
}