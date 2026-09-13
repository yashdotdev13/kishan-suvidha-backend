package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(
            ErrorCode errorCode,
            String message
    ) {
        super(errorCode, message);
    }
}