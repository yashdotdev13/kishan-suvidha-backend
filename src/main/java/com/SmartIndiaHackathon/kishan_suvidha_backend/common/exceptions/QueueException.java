package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

public class QueueException extends BadRequestException {

    public QueueException(
            ErrorCode errorCode,
            String message
    ) {
        super(message);
    }
}