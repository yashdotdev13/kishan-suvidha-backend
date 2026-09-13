package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(
            ErrorCode errorCode,
            String message
    ) {
        super(errorCode, message);
    }
}