package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProcurementCentreRequest(

        @NotBlank(message = "Centre name is required")
        String name,

        @NotBlank(message = "State is required")
        String state,

        @NotBlank(message = "District is required")
        String district,

        @NotBlank(message = "Location is required")
        String location,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity
) {
}