package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos;



import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.enums.QueueStatus;

import java.time.LocalDateTime;

public record QueueEntryResponse(
        Long id,
        Long cropId,
        Long procurementCentreId,
        String procurementCentreName,
        Integer tokenNumber,
        Integer currentToken,
        Integer farmersAhead,
        Integer estimatedWaitMinutes,
        QueueStatus status,
        LocalDateTime joinedAt,
        LocalDateTime servedAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt
) {
}