package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MandiPriceBacktestResponse(
        String cropName,
        String marketName,
        String state,
        LocalDate fromDate,
        LocalDate toDate,
        int evaluationCount,
        BigDecimal meanAbsoluteError,
        BigDecimal meanAbsolutePercentageError,
        BigDecimal rootMeanSquaredError,
        String method,
        List<String> limitations
) {
    public MandiPriceBacktestResponse {
        limitations = limitations == null
                ? List.of()
                : List.copyOf(limitations);
    }
}