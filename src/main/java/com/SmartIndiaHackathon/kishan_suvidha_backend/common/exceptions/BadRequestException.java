package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }

    public BadRequestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}