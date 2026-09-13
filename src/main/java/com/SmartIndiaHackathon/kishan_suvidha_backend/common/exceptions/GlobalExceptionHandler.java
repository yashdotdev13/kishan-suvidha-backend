package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getErrorCode(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            BadRequestException ex
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getErrorCode(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            ConflictException ex
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getErrorCode(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            UnauthorizedException ex
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getErrorCode(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            ForbiddenException ex
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                ex.getErrorCode(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException exception
    ) {
        log.warn(
                "Access denied: {}",
                exception.getMessage()
        );

        return buildResponse(
                HttpStatus.FORBIDDEN,
                ErrorCode.FORBIDDEN,
                "You do not have permission to access this resource"
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException exception
    ) {
        log.warn(
                "Authentication failed: {}",
                exception.getMessage()
        );

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED,
                "Authentication is required"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception ex
    ) {
        /*
         * Log the complete exception and stack trace.
         * The client still receives a generic message so we don't
         * expose internal implementation details.
         */
        log.error(
                "Unexpected error occurred while processing request",
                ex
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                false,
                errorCode,
                message
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}