package com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.dtos;

import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.enums.OfferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OfferResponse(
        Long id,
        Long cropId,
        String cropName,
        Long buyerId,
        BigDecimal quantity,
        BigDecimal offeredPrice,
        BigDecimal counterPrice,
        OfferStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}