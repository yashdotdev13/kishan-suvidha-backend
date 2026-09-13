package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReferencePriceResponse(
        Long id,
        String cropName,
        CropType cropType,
        BigDecimal pricePerQuintal,
        String state,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}