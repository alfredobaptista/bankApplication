package com.github.freddy.bankApi.docs;

import com.github.freddy.bankApi.dto.UpdateStatusRequest;
import com.github.freddy.bankApi.dto.response.AccountResponse;
import com.github.freddy.bankApi.dto.response.ApiResponseDTO;
import com.github.freddy.bankApi.dto.response.BalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Interface de documentação OpenAPI/Swagger para o controller de contas.
 * <p>
 * Contém operações relacionadas à consulta e gestão de contas bancárias.
 */
@Tag(
        name = "Contas Bancárias"
)

@SecurityRequirement(name = "bearerAuth")  // Assume que usas JWT com security scheme "bearerAuth"
public interface AccountControllerDocs {

    @Operation(
            summary = "Consultar detalhes da conta",
            description = "Permite a um utilizador STAFF consultar os detalhes completos de uma conta específica pelo número da conta"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalhes da conta recuperados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Número da conta inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado – utilizador sem permissão STAFF"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    ResponseEntity<ApiResponseDTO<AccountResponse>> getAccountDetails(
            @Parameter(description = "Número da conta (ex: 1234567890123456)", example = "1234567890123456")
            @PathVariable String accountNumber,

            HttpServletRequest request
    );

    @Operation(
            summary = "Consultar saldo da conta",
            description = "Retorna o saldo atual da conta do utilizador autenticado (apenas para o próprio cliente)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo consultado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado – apenas clientes podem consultar o próprio saldo")
    })
    ResponseEntity<ApiResponseDTO<BalanceResponse>> getBalance(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    );

    @Operation(
            summary = "Actualizar status da conta",
            description = "Altera o status de uma conta (ex: ACTIVE → BLOCKED, SUSPENDED, etc.). " +
                    "Apenas STAFF e ADMIN podem executar esta operação"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status da conta atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou status não permitido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado – requer permissão STAFF ou ADMIN"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    ResponseEntity<ApiResponseDTO<Void>> updateStatus(
            @Parameter(description = "Número da conta a ser actualizada", example = "A01000012")
            @PathVariable String number,

            @Parameter(description = "Novo status da conta")
            @RequestBody @Valid UpdateStatusRequest request,

            HttpServletRequest httpRequest
    );
}