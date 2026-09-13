package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

public class ConflictException extends ApiException {

    public ConflictException(
            ErrorCode errorCode,
            String message
    ) {
        super(errorCode, message);
    }
}