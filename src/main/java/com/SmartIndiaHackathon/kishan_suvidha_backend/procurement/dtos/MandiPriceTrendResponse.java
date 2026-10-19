package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MandiPriceTrendResponse(
        String cropName,
        String marketName,
        String state,
        LocalDate latestObservationDate,
        BigDecimal latestModalPricePerQuintal,
        BigDecimal averagePrice7Days,
        BigDecimal priceChange7Days,
        BigDecimal priceChangePercentage7Days,
        BigDecimal averagePrice30Days,
        BigDecimal priceChange30Days,
        BigDecimal priceChangePercentage30Days,
        BigDecimal minimumPrice,
        BigDecimal maximumPrice,
        BigDecimal averageHistoricalPrice,
        long observationCount,
        LocalDate firstObservationDate,
        LocalDate lastObservationDate,
        String trend,
        String message
) {
}
