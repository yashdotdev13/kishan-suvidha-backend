package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

public class OfferException extends BadRequestException {

    public OfferException(
            ErrorCode errorCode,
            String message
    ) {
        super(message);
    }
}