package com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos;

import java.math.BigDecimal;

public record TransportCostEstimateResponse(
        Long procurementCentreId,
        Long transportOptionId,
        BigDecimal quantityQuintals,
        BigDecimal distanceKm,
        BigDecimal ratePerKmPerQuintal,
        BigDecimal estimatedTransportCost
) {
}