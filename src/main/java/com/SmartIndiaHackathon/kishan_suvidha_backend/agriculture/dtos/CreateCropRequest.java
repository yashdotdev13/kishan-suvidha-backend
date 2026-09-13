package com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.dtos;


import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropType;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCropRequest(

        @NotBlank(message = "Crop name is required")
        String cropName,

        @NotNull(message = "Crop type is required")
        CropType cropType,

        @NotNull(message = "Quantity is required")
        @DecimalMin(
                value = "0.001",
                message = "Quantity must be greater than zero"
        )
        BigDecimal quantity,

        @NotNull(message = "Unit is required")
        CropUnit unit,

        @NotNull(message = "Expected price is required")
        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Expected price cannot be negative"
        )
        BigDecimal expectedPrice,

        @NotNull(message = "Harvest date is required")
        @PastOrPresent(message = "Harvest date cannot be in the future")
        LocalDate harvestDate
) {
}