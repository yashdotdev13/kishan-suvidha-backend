package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

import java.util.Map;

public class ValidationException extends ApiException {

    private final Map<String, String> errors;

    public ValidationException(
            String message,
            Map<String, String> errors
    ) {
        super(ErrorCode.VALIDATION_FAILED, message);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}