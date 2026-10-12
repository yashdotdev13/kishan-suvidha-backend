
package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateMandiPriceObservationRequest(

        @NotBlank(message = "Crop name is required")
        String cropName,

        @NotBlank(message = "Market name is required")
        String marketName,

        @NotBlank(message = "State is required")
        String state,

        @NotNull(message = "Observation date is required")
        @PastOrPresent(message = "Observation date cannot be in the future")
        LocalDate observationDate,

        @DecimalMin(value = "0.00", message = "Minimum price cannot be negative")
        BigDecimal minPricePerQuintal,

        @NotNull(message = "Modal price is required")
        @DecimalMin(value = "0.01", message = "Modal price must be positive")
        BigDecimal modalPricePerQuintal,

        @DecimalMin(value = "0.00", message = "Maximum price cannot be negative")
        BigDecimal maxPricePerQuintal,

        @NotBlank(message = "Source is required")
        String source,

        @NotBlank(message = "Source record ID is required")
        String sourceRecordId
) {
}
