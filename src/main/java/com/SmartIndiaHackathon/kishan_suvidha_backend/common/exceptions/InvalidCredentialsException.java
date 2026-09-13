package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

public class InvalidCredentialsException extends UnauthorizedException {

    public InvalidCredentialsException() {
        super(
                ErrorCode.INVALID_CREDENTIALS,
                "Invalid email or password"
        );
    }

    public InvalidCredentialsException(String message) {
        super(
                ErrorCode.INVALID_CREDENTIALS,
                message
        );
    }
}