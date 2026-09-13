package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos;


import jakarta.validation.constraints.NotNull;

public record JoinQueueRequest(

        @NotNull(message = "Crop ID is required")
        Long cropId,

        @NotNull(message = "Procurement centre ID is required")
        Long procurementCentreId
) {
}