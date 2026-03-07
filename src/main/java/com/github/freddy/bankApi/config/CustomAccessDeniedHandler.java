package com.github.freddy.bankApi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freddy.bankApi.dto.response.ApiResponseError;
import com.github.freddy.bankApi.enums.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
       response.setContentType("application/json");
       response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ApiResponseError error = new ApiResponseError(
                HttpStatus.FORBIDDEN.value(),
                ErrorCode.UNAUTHORIZED,
                "Não tem permissão",
                request.getRequestURI(),
                OffsetDateTime.now(),
                null
        );
        response.getWriter().println(objectMapper.writeValueAsString(error));
    }
}
