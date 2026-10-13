package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SaleTimingRecommendationResponse(
        String cropName,
        String marketName,
        String state,
        BigDecimal currentModalPricePerQuintal,
        LocalDate currentPriceDate,
        BigDecimal predictedModalPricePerQuintal,
        LocalDate forecastDate,
        BigDecimal expectedChangePercentage,
        Recommendation recommendation,
        String rationale,
        String uncertaintyDescription,
        List<String> limitations
) {
    public enum Recommendation {
        SELL_NOW,
        WAIT_AND_MONITOR,
        INSUFFICIENT_DATA
    }
}