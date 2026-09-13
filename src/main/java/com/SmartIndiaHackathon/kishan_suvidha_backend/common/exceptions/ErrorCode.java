package com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // General
    INTERNAL_SERVER_ERROR,
    BAD_REQUEST,
    VALIDATION_FAILED,
    RESOURCE_NOT_FOUND,
    CONFLICT,
    UNAUTHORIZED,
    FORBIDDEN,

    // Authentication
    USER_ALREADY_EXISTS,
    INVALID_CREDENTIALS,
    INVALID_TOKEN,
    TOKEN_EXPIRED,

    // Agriculture
    CROP_NOT_FOUND,
    CROP_NOT_AVAILABLE,
    INVALID_CROP_QUANTITY,

    // Procurement
    PROCUREMENT_CENTRE_NOT_FOUND,
    QUEUE_NOT_FOUND,
    QUEUE_FULL,
    ALREADY_IN_QUEUE,
    QUEUE_NOT_ACCEPTING,
    INVALID_QUEUE_OPERATION,

    // Marketplace
    OFFER_NOT_FOUND,
    OFFER_EXPIRED,
    OFFER_ALREADY_RESPONDED,
    INVALID_OFFER_OPERATION,

    // Transaction
    TRANSACTION_NOT_FOUND,
    INVALID_TRANSACTION_STATE,
    TRANSACTION_ALREADY_COMPLETED,

    // Logistics
    TRANSPORT_NOT_FOUND,
    SHIPMENT_NOT_FOUND,
    TRANSPORT_UNAVAILABLE,
    INVALID_SHIPMENT_OPERATION
}