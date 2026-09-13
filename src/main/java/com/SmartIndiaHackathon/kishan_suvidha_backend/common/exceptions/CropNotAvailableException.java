package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

public class CropNotAvailableException extends ConflictException {

    public CropNotAvailableException(String message) {
        super(
                ErrorCode.CROP_NOT_AVAILABLE,
                message
        );
    }
}