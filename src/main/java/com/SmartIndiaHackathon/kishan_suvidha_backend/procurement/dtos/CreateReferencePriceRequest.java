package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateReferencePriceRequest(

        @NotBlank(message = "Crop name is required")
        String cropName,

        @NotNull(message = "Crop type is required")
        CropType cropType,

        @NotNull(message = "Price per quintal is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal pricePerQuintal,

        @NotBlank(message = "State is required")
        String state,

        @NotNull(message = "Effective from date is required")
        LocalDate effectiveFrom,

        LocalDate effectiveTo
) {
}