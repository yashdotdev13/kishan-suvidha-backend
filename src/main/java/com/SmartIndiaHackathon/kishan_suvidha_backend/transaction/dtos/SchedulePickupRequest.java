package com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SchedulePickupRequest(

        @NotNull(message = "Pickup date and time is required")
        LocalDateTime pickupScheduledAt

) {
}