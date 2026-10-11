package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.dtos;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateAggregationDemandRequest(

        @NotBlank(message = "Crop name is required")
        @Size(max = 100, message = "Crop name must not exceed 100 characters")
        String cropName,

        @NotNull(message = "Crop type is required")
        CropType cropType,

        @NotNull(message = "Target quantity in kilograms is required")
        @DecimalMin(
                value = "0.001",
                message = "Target quantity must be greater than zero"
        )
        BigDecimal targetQuantityKg,

        @DecimalMin(
                value = "0.01",
                message = "Maximum price per kilogram must be positive"
        )
        BigDecimal maxPricePerKg,

        @NotBlank(message = "Delivery location is required")
        @Size(max = 300, message = "Delivery location must not exceed 300 characters")
        String deliveryLocation,

        @DecimalMin(value = "-90.0", message = "Latitude must not be below -90")
        @jakarta.validation.constraints.DecimalMax(
                value = "90.0",
                message = "Latitude must not exceed 90"
        )
        Double deliveryLatitude,

        @DecimalMin(value = "-180.0", message = "Longitude must not be below -180")
        @jakarta.validation.constraints.DecimalMax(
                value = "180.0",
                message = "Longitude must not exceed 180"
        )
        Double deliveryLongitude,

        @FutureOrPresent(message = "Required-by date cannot be in the past")
        LocalDate requiredBy

){

}