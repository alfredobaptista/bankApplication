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

    private ResponseEntity<ApiResponseError> buildError(HttpStatus status,
                                                        ErrorCode errorCode,
                                                        String message,
                                                        HttpServletRequest request,
                                                        List<ApiResponseError.FieldErrorDetail> details) {
        return ResponseEntity.status(status)
                .body(new ApiResponseError(
                        status.value(),
                        errorCode,
                        message,
                        request.getRequestURI(),
                        OffsetDateTime.now(),
                        details
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseError> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        var details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ApiResponseError.FieldErrorDetail(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                "Erro de validação nos campos enviados", request, details);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseError> handleBadCredentials(BadCredentialsException ex,
                                                                 HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseError> handleUnauthorized(UnauthorizedException ex,
                                                               HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponseError> handleNotFound(NotFoundException ex,
                                                           HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponseError> handleInsufficientBalance(InsufficientBalanceException ex,
                                                                      HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ErrorCode.INSUFFICIENT_FUNDS, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponseError> handleConflict(ConflictException ex,
                                                           HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ErrorCode.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(AccountCreationException.class)
    public ResponseEntity<ApiResponseError> handleAccountCreation(AccountCreationException ex,
                                                                  HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.ACCOUNT_CREATION_FAILED, ex.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidReferenceCodeException.class)
    public ResponseEntity<ApiResponseError> handleInvalidReferenceCode(InvalidReferenceCodeException ex,
                                                                       HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REFERENCE_CODE, ex.getMessage(), request, null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseError> handleRuntime(RuntimeException ex,
                                                          HttpServletRequest request) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR,
                "Erro interno do servidor", request, null);
    }
}