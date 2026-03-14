package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.docs.AtmControllerDocs;
import com.github.freddy.bankApi.dto.request.AtmCardlessRequest;
import com.github.freddy.bankApi.dto.response.AtmCardlessResponse;
import com.github.freddy.bankApi.dto.response.ApiResponseDTO;
import com.github.freddy.bankApi.service.AtmService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/atm")
@RequiredArgsConstructor
public class AtmController implements AtmControllerDocs {

    private final AtmService atmService;

    @Override
    @PostMapping("/cardless")
    public ApiResponseDTO<AtmCardlessResponse> completeCardless(
            @RequestBody AtmCardlessRequest dto,
            HttpServletRequest request) {
        AtmCardlessResponse response = atmService.cardlessWithdraw(dto);
        return new ApiResponseDTO<>(
                true,
                "Levantamento realizado",
                response,
                null,
                request.getRequestURI(),
                OffsetDateTime.now()
        );
    }
}