package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.MandiPriceObservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MandiPriceObservationResponse(
        Long id,
        String cropName,
        String marketName,
        String state,
        LocalDate observationDate,
        BigDecimal minPricePerQuintal,
        BigDecimal modalPricePerQuintal,
        BigDecimal maxPricePerQuintal,
        String source,
        String sourceRecordId,
        LocalDateTime createdAt
) {
    public static MandiPriceObservationResponse from(
            MandiPriceObservation observation
    ) {
        return new MandiPriceObservationResponse(
                observation.getId(),
                observation.getCropName(),
                observation.getMarketName(),
                observation.getState(),
                observation.getObservationDate(),
                observation.getMinPricePerQuintal(),
                observation.getModalPricePerQuintal(),
                observation.getMaxPricePerQuintal(),
                observation.getSource(),
                observation.getSourceRecordId(),
                observation.getCreatedAt()
        );
    }
}