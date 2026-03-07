package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.dto.request.AtmCompleteCardlessRequest;
import com.github.freddy.bankApi.dto.response.ApiResponse;
import com.github.freddy.bankApi.dto.response.AtmCompleteResponse;
import com.github.freddy.bankApi.service.AtmService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/atm")
@RequiredArgsConstructor
public class AtmController {
    private final AtmService atmService;

    @PostMapping("/cardless")
    public ResponseEntity<ApiResponse<AtmCompleteResponse>> completeCardless(
            @RequestBody AtmCompleteCardlessRequest dto,
            HttpServletRequest request
    ) {
        AtmCompleteResponse response = atmService.completeCardlessWithdrawAtAtm(dto);
        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "Levantamento Realizado",
                        response,
                        null,
                        request.getRequestURI(),
                        OffsetDateTime.now()
                )
        );
    }
}