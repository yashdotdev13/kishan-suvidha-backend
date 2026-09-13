package com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOfferRequest(

        @NotNull(message = "Crop ID is required")
        Long cropId,

        @NotNull(message = "Quantity is required")
        @DecimalMin(
                value = "0.001",
                message = "Quantity must be greater than zero"
        )
        BigDecimal quantity,

        @NotNull(message = "Offered price is required")
        @DecimalMin(
                value = "0.01",
                message = "Offered price must be greater than zero"
        )
        BigDecimal offeredPrice
) {
}