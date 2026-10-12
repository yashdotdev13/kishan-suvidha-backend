package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MandiPriceForecastResponse(
        String cropName,
        String marketName,
        String state,
        LocalDate forecastDate,
        BigDecimal predictedModalPricePerQuintal,
        BigDecimal lowerPredictionBound,
        BigDecimal upperPredictionBound,
        String method,
        int trainingObservationCount,
        String uncertaintyDescription,
        List<String> limitations
) {
    public MandiPriceForecastResponse {
        limitations = limitations == null
                ? List.of()
                : List.copyOf(limitations);
    }
}