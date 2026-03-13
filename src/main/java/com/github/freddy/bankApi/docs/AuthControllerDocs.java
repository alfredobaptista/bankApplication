package com.github.freddy.bankApi.docs;

import com.github.freddy.bankApi.dto.request.*;
import com.github.freddy.bankApi.dto.response.ApiResponseDTO;
import com.github.freddy.bankApi.dto.response.AuthTokensResponse;
import com.github.freddy.bankApi.dto.response.RegistrationResponse;
import com.github.freddy.bankApi.dto.response.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;


@Tag(
        name = "Autenticação"
)
public interface AuthControllerDocs {

    @Operation(summary = "Registra novo cliente", description = "Registra um novo usuário cliente e cria conta padrão")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou BI já cadastrado")
    })
    ResponseEntity<ApiResponseDTO<RegistrationResponse>> register(RegisterRequest data, HttpServletRequest request, UriComponentsBuilder uriBuilder);

    @Operation(summary = "Login do usuário", description = "Autentica usuário e retorna access + refresh tokens JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login efetuado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    ResponseEntity<ApiResponseDTO<AuthTokensResponse> >login(LoginRequest data, HttpServletRequest request);

    @Operation(
            summary = "Refresh tokens",
            description = "Gera novos access e refresh tokens usando um refresh token válido"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens emitidos com sucesso"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    })
    ResponseEntity<ApiResponseDTO<AuthTokensResponse>> refreshToken(RefreshTokenRequest data, HttpServletRequest request);



    @Operation(
            summary = "Logout do usuário",
            description = "Revoga todos os refresh tokens do usuário logado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout efetuado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> logout(
            @AuthenticationPrincipal Jwt jwt,
            LogoutRequest data
    );


    @Operation(
            summary = "Registo de Funcionario",
            description = "Cria um novo stff ou admin",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilizador Criado com Sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<ApiResponseDTO<UserProfileResponse>> createInternalUser(
            @Valid @RequestBody StaffUserRequest dto,
            UriComponentsBuilder uriBuilder,
            HttpServletRequest request
    );
}