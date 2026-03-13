package com.github.freddy.bankApi.controller;
import com.github.freddy.bankApi.dto.request.AtmCardlessRequest;
import com.github.freddy.bankApi.dto.response.AtmCardlessResponse;
import com.github.freddy.bankApi.dto.response.ApiResponseDTO;
import com.github.freddy.bankApi.exception.InvalidReferenceCodeException;
import com.github.freddy.bankApi.exception.NotFoundException;
import com.github.freddy.bankApi.service.AtmService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtmController  Endpoint cardless withdrawal")
class AtmControllerTest {

    @Mock
    private AtmService atmService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AtmController atmController;

    private static final String REQUEST_URI = "/api/v1/atm/cardless";
    private static final String REFERENCE_CODE = "REF-TEST-987654";
    private static final String SECRET_CODE = "963852";
    private static final BigDecimal AMOUNT = new BigDecimal("12000.00");
    private static final String TRANSACTION_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setupMocks() {
        when(httpRequest.getRequestURI()).thenReturn(REQUEST_URI);
    }

    @Nested
    @DisplayName("Caso de sucesso")
    class SuccessCases {

        @Test
        @DisplayName("Deve processar levantamento cardless e retornar ApiResponseDTO com sucesso")
        void shouldCompleteCardlessWithdrawalAndReturnSuccessResponse() {
            // Arrange
            AtmCardlessRequest requestDto = new AtmCardlessRequest(REFERENCE_CODE, SECRET_CODE);

            AtmCardlessResponse serviceResponse = new AtmCardlessResponse(
                    TRANSACTION_ID,
                    AMOUNT,
                    OffsetDateTime.now().minusSeconds(5) // ligeiramente anterior para simular
            );

            when(atmService.cardlessWithdrawAtAtm(any(AtmCardlessRequest.class)))
                    .thenReturn(serviceResponse);

            // Act
            ApiResponseDTO<AtmCardlessResponse> apiResponse =
                    atmController.completeCardless(requestDto, httpRequest);

            // Assert
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.isSuccess()).isTrue();
            assertThat(apiResponse.message()).isEqualTo("Levantamento realizado");
            assertThat(apiResponse.data()()).isSameAs(serviceResponse);
            assertThat(apiResponse.getErrors()).isNull();
            assertThat(apiResponse.getPath()).isEqualTo(REQUEST_URI);
            assertThat(apiResponse.getTimestamp()).isNotNull();

            // Verifica que o serviço foi chamado exatamente 1x com o DTO correto
            verify(atmService).cardlessWithdrawAtAtm(requestDto);
            verifyNoMoreInteractions(atmService);
        }
    }

    @Nested
    @DisplayName("Casos de erro / exceções")
    class ErrorCases {

        @Test
        @DisplayName("Deve propagar NotFoundException do serviço")
        void shouldPropagateNotFoundExceptionFromService() {
            // Arrange
            AtmCardlessRequest requestDto = new AtmCardlessRequest("INVALID-REF", SECRET_CODE);

            when(atmService.cardlessWithdrawAtAtm(any(AtmCardlessRequest.class)))
                    .thenThrow(new NotFoundException("Código de referência inválido"));

            // Act & Assert
            assertThatThrownBy(() -> atmController.completeCardless(requestDto, httpRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Código de referência inválido");

            verify(atmService).cardlessWithdrawAtAtm(requestDto);
            verifyNoMoreInteractions(atmService);
        }

        @Test
        @DisplayName("Deve propagar InvalidReferenceCodeException do serviço")
        void shouldPropagateInvalidReferenceCodeExceptionFromService() {
            // Arrange
            AtmCardlessRequest requestDto = new AtmCardlessRequest(REFERENCE_CODE, "999999");

            when(atmService.cardlessWithdrawAtAtm(any(AtmCardlessRequest.class)))
                    .thenThrow(new InvalidReferenceCodeException("Código inválido"));

            // Act & Assert
            assertThatThrownBy(() -> atmController.completeCardless(requestDto, httpRequest))
                    .isInstanceOf(InvalidReferenceCodeException.class)
                    .hasMessage("Código inválido");

            verify(atmService).cardlessWithdrawAtAtm(requestDto);
            verifyNoMoreInteractions(atmService);
        }

        @Test
        @DisplayName("Deve lidar com request body nulo (validação do framework)")
        void shouldHandleNullRequestBody() {
            // Neste caso o Spring lança MethodArgumentNotValidException ou HttpMessageNotReadableException
            // Mas em teste unitário puro, simulamos passando null
            assertThatThrownBy(() -> atmController.completeCardless(null, httpRequest))
                    .isInstanceOf(NullPointerException.class); // ou outra exceção dependendo da config
            // Nota: em testes de integração com @WebMvcTest seria melhor testar validação @Valid
        }
    }

    @Nested
    @DisplayName("Verificações adicionais de estrutura")
    class ResponseStructure {

        @Test
        @DisplayName("A resposta deve ter timestamp recente")
        void responseShouldHaveRecentTimestamp() {
            AtmCardlessRequest dto = new AtmCardlessRequest(REFERENCE_CODE, SECRET_CODE);
            AtmCardlessResponse mockResponse = new AtmCardlessResponse(TRANSACTION_ID, AMOUNT, OffsetDateTime.now().toLocalDateTime());

            when(atmService.cardlessWithdrawAtAtm(dto)).thenReturn(mockResponse);

            ApiResponseDTO<AtmCardlessResponse> apiResponse = atmController.completeCardless(dto, httpRequest);

            OffsetDateTime timestamp = apiResponse.timestamp();
            assertThat(timestamp).isNotNull();
            assertThat(timestamp).isAfter(OffsetDateTime.now().minusSeconds(10));
            assertThat(timestamp).isBefore(OffsetDateTime.now().plusSeconds(5));
        }
    }
}