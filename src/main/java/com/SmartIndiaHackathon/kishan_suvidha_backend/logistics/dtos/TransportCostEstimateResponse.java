package com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos;


import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.enums.TransportRouteType;

import java.math.BigDecimal;

public record TransportCostEstimateResponse(
        TransportRouteType routeType,
        Long destinationId,
        Long transportOptionId,
        BigDecimal quantityQuintals,
        BigDecimal distanceKm,
        BigDecimal ratePerKmPerQuintal,
        BigDecimal estimatedTransportCost
) {
}