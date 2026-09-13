package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

public class ShipmentException extends BadRequestException {

    public ShipmentException(
            ErrorCode errorCode,
            String message
    ) {
        super(message);
    }
}