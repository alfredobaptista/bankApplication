package com.github.freddy.bankApi.docs;

import com.github.freddy.bankApi.dto.request.AtmCardlessRequest;
import com.github.freddy.bankApi.dto.response.AtmCardlessResponse;
import com.github.freddy.bankApi.dto.response.ApiResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
@Tag(
        name = "Levantamentos Sem Cartão"
)
public interface AtmControllerDocs {

    @Operation(
            summary = "Levantamento sem cartão",
            description = "Permite ao cliente levantar dinheiro no ATM sem cartão usando um código de referência")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Levantamento realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Código de referência inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Conta ou levantamento não encontrado")
    })
    ApiResponseDTO<AtmCardlessResponse> completeCardless(AtmCardlessRequest dto, HttpServletRequest request);
}