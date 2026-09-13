package com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.dtos;

import com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(

        Long id,

        Long offerId,

        Long cropId,

        String cropName,

        Long farmerId,

        Long buyerId,

        Long transportOptionId,

        String vehicleNumber,

        BigDecimal quantity,

        BigDecimal agreedPrice,

        BigDecimal grossAmount,

        BigDecimal distanceKm,

        BigDecimal transportCost,

        BigDecimal netAmount,

        TransactionStatus status,

        LocalDateTime pickupScheduledAt,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        LocalDateTime completedAt,

        LocalDateTime cancelledAt
) {
}