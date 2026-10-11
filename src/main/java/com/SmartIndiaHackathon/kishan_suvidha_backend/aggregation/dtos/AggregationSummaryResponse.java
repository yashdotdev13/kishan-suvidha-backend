package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.dtos;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationDemandStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationPlanStatus;

import java.math.BigDecimal;
import java.util.List;

public record AggregationSummaryResponse(
        Long demandId,
        String cropName,
        BigDecimal requestedQuantityKg,
        BigDecimal plannedQuantityKg,
        BigDecimal acceptedQuantityKg,
        BigDecimal reservedQuantityKg,
        BigDecimal remainingQuantityKg,
        AggregationDemandStatus demandStatus,
        List<PlanSummary> plans
) {
    public record PlanSummary(
            Long planId,
            AggregationPlanStatus status,
            BigDecimal plannedQuantityKg,
            BigDecimal acceptedQuantityKg,
            BigDecimal reservedQuantityKg
    ) {
    }
}