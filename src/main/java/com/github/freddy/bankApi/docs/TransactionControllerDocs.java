package com.github.freddy.bankApi.docs;

import com.github.freddy.bankApi.dto.request.CardlessWithdrawRequest;
import com.github.freddy.bankApi.dto.request.DepositRequest;
import com.github.freddy.bankApi.dto.request.TransferRequest;
import com.github.freddy.bankApi.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(
        name = "Transações"
)
public interface TransactionControllerDocs {

    @Operation(summary = "Transferência entre contas", description = "Permite que o usuário faça uma transferência entre contas")
    @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso")
    ResponseEntity<ApiResponseDTO<TransferResponse>> transfer(@RequestBody TransferRequest dto,
                            @AuthenticationPrincipal Jwt jwt,
                            HttpServletRequest request);

    @Operation(summary = "Depósito em conta", description = "Permite que um staff faça um depósito em uma conta específica")
    @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso")
   ResponseEntity< ApiResponseDTO<DepositResponse> >deposit(@RequestBody DepositRequest dto,
                                            @AuthenticationPrincipal Jwt jwt,
                                            HttpServletRequest request);

    @Operation(summary = "Levantamento sem cartão", description = "Permite que o usuário faça um levantamento sem cartão da sua conta")
    @ApiResponse(responseCode = "200", description = "Levantamento realizado com sucesso")
    ResponseEntity<ApiResponseDTO<CardlessWithdrawResponse>> withdraw(@RequestBody CardlessWithdrawRequest dto,
                                                      @AuthenticationPrincipal Jwt jwt,
                                                      HttpServletRequest request);

    @Operation(
            summary = "Lista levantamentos sem cartão",
            description = "Lista todos os levantamentos sem cartão do usuário logado")
    @ApiResponse(responseCode = "200", description = "Lista obtida com sucesso")
    ResponseEntity<ApiResponseDTO<List<CardlessWithdrawalDetailsResponse>>> listAllCardless(int page, int size,
                                                                            @AuthenticationPrincipal Jwt jwt,
                                                                            HttpServletRequest request);

    @Operation(
            summary = "Cancela levantamento sem cartão",
            description = "Permite cancelar um levantamento sem cartão antes de ser retirado")
    @ApiResponse(responseCode = "204", description = "Levantamento cancelado com sucesso")
    ResponseEntity<Void> cancelWithDrawal(
            @PathVariable String referenceCode,
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(summary = "Histórico de transações", description = "Retorna o histórico completo de transações da conta do usuário logado")
    @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso")
    ResponseEntity<ApiResponseDTO<List<TransactionResponse>>> getTransactionHistory(
                                                                    int page, int size, String sort,
                                                                    @AuthenticationPrincipal Jwt jwt,
                                                                    HttpServletRequest request);
}