package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

public class UserAlreadyExistsException extends ConflictException {

    public UserAlreadyExistsException(String message) {
        super(ErrorCode.USER_ALREADY_EXISTS, message);
    }
}