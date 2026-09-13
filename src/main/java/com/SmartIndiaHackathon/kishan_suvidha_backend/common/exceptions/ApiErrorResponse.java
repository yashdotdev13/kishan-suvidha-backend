package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        boolean success,
        ErrorCode errorCode,
        String message,
        LocalDateTime timestamp
) {

    public ApiErrorResponse(
            boolean success,
            ErrorCode errorCode,
            String message
    ) {
        this(
                success,
                errorCode,
                message,
                LocalDateTime.now()
        );
    }
}