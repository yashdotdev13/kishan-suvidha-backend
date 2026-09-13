package com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos;

import jakarta.validation.constraints.NotNull;

public record AssignTransportRequest(

        @NotNull(message = "Transport option ID is required")
        Long transportOptionId
) {
}