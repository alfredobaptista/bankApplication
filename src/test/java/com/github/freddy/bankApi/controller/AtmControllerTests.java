package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.dto.request.AtmCardlessRequest;
import com.github.freddy.bankApi.dto.response.AtmCardlessResponse;
import com.github.freddy.bankApi.factory.AtmCardlessResponseFactory;
import com.github.freddy.bankApi.factory.CardlessRequestFactory;
import com.github.freddy.bankApi.service.AtmService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AtmControllerTests {

    @Mock
    private AtmService atmService;

    @InjectMocks
    private AtmController atmController;

    ArgumentCaptor<AtmCardlessRequest> atmRequestCaptor = ArgumentCaptor.forClass(AtmCardlessRequest.class);

    @Nested
    class CompleteCardless {
        @Test
        void shouldReturnSucessTrue()  {
            //ARRANGE
            HttpServletRequest request = new MockHttpServletRequest();
            doReturn(AtmCardlessResponseFactory.createCardlessResponse())
                    .when(atmService).cardlessWithdraw(any(AtmCardlessRequest.class));
            //ACT
            var response = atmController.completeCardless(CardlessRequestFactory.createAtmCardlessRequest(), request);

            //ASSERT
            assertTrue(response.success());
            assertEquals(BigDecimal.valueOf(2000.00), response.data().amountWithdrawn());
            assertEquals("12345", response.data().transactionId());
            verify(atmService, times(1)).cardlessWithdraw(any(AtmCardlessRequest.class));

        }

        @Test
        void shouldPassCorrectParameterToService()  {
            //ARRANGE
            HttpServletRequest request = new MockHttpServletRequest();
            doReturn(AtmCardlessResponseFactory.createCardlessResponse())
                    .when(atmService).cardlessWithdraw(atmRequestCaptor.capture());
            //ACT
            var response = atmController.completeCardless(CardlessRequestFactory.createAtmCardlessRequest(), request);
            //ASSERT
            assertEquals(CardlessRequestFactory.createAtmCardlessRequest(), atmRequestCaptor.getValue());
            assertEquals(1, atmRequestCaptor.getAllValues().size());
        }

        @Test
        void shouldReturnCorrectResponseBody() {
            //ACT
            MockHttpServletRequest request = new MockHttpServletRequest();
            LocalDateTime now = LocalDateTime.now();
            AtmCardlessResponse cardlessResponse = AtmCardlessResponseFactory.createCardlessResponse();

            doReturn(AtmCardlessResponseFactory.createCardlessResponse()).when(atmService).cardlessWithdraw(any(AtmCardlessRequest.class));
            //ARRANGE
            var response = atmController.completeCardless(CardlessRequestFactory.createAtmCardlessRequest(), request);

            //ASSERT
            assertNotNull(response);
            assertEquals(AtmCardlessResponseFactory.createCardlessResponse().amountWithdrawn(), response.data().amountWithdrawn());
            assertEquals("12345", response.data().transactionId());

        }
    }
}
