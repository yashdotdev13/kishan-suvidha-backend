package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropUnit;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CropQuantityConverter {

    private static final BigDecimal KG_PER_QUINTAL =
            BigDecimal.valueOf(100);

    private static final BigDecimal KG_PER_TON =
            BigDecimal.valueOf(1000);

    public BigDecimal toKilograms(
            BigDecimal quantity,
            CropUnit unit
    ) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Crop quantity must be greater than zero");
        }

        if (unit == null) {
            throw new IllegalArgumentException(
                    "Crop quantity unit is required");
        }

        BigDecimal kilograms = switch (unit) {
            case KG -> quantity;
            case QUINTAL -> quantity.multiply(KG_PER_QUINTAL);
            case TON -> quantity.multiply(KG_PER_TON);
        };

        return kilograms.setScale(3, RoundingMode.HALF_UP);
    }
}