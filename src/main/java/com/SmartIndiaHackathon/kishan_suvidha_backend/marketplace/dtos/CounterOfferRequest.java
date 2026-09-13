package com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CounterOfferRequest(

        @NotNull(message = "Counter price is required")
        @DecimalMin(
                value = "0.01",
                message = "Counter price must be greater than zero"
        )
        BigDecimal counterPrice
) {
}