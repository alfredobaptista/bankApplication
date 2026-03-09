package com.github.freddy.bankApi.exception;

import com.github.freddy.bankApi.dto.response.ApiResponseError;
import com.github.freddy.bankApi.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ResponseEntity<ApiResponseError> buildErrorResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            HttpServletRequest request,
            List<ApiResponseError.FieldErrorDetail> details
    ) {
        var response = new ApiResponseError(
                status.value(),
                errorCode,
                message,
                request.getRequestURI(),
                OffsetDateTime.now(),
                details
        );
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        var fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ApiResponseError.FieldErrorDetail(
                        fe.getField(),
                        fe.getDefaultMessage())
                ).toList();

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Um ou mais campos não passaram na validação",
                request,
                fieldErrors
        );
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseError> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, ex.getMessage(), request, null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseError> handleUnauthorizedException(
            UnauthorizedException ex, HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponseError> handleNotFoundException(
            NotFoundException ex, HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponseError> handleInsufficientBalanceException(
            InsufficientBalanceException ex, HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.INSUFFICIENT_FUNDS,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponseError> handleConflictException(
            ConflictException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.VALIDATION_ERROR,
                ex.getMessage(),
                request,
                null
                );
    }


    @ExceptionHandler(AccountCreationException.class)
    public ResponseEntity<ApiResponseError> handleAccountCreationException(
            AccountCreationException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.UNAUTHORIZED,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(InvalidReferenceCodeException.class)
    public ResponseEntity<ApiResponseError> handleInvalidReferenceCodeException(
            InvalidReferenceCodeException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INSUFFICIENT_FUNDS,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseError> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        logger.error(ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Erro interno do servidor",
                request,
                null
        );
    }
}


