package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

public class TransactionException extends BadRequestException {

    public TransactionException(
            ErrorCode errorCode,
            String message
    ) {
        super(message);
    }
}