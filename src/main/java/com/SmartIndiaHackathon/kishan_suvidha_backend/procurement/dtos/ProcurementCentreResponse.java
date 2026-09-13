package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos;


import java.time.LocalDateTime;

public record ProcurementCentreResponse(
        Long id,
        String name,
        String state,
        String district,
        String location,
        Double latitude,
        Double longitude,
        Integer capacity,
        Integer currentToken,
        Boolean acceptingQueue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}