package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation;


import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.service.CropQuantityConverter;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CropQuantityConverterTest {

    private CropQuantityConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CropQuantityConverter();
    }

    @Test
    void shouldKeepKilogramsUnchanged() {
        BigDecimal result = converter.toKilograms(
                new BigDecimal("125.500"),
                CropUnit.KG
        );

        assertEquals(new BigDecimal("125.500"), result);
    }

    @Test
    void shouldConvertQuintalsToKilograms() {
        BigDecimal result = converter.toKilograms(
                new BigDecimal("25.000"),
                CropUnit.QUINTAL
        );

        assertEquals(new BigDecimal("2500.000"), result);
    }

    @Test
    void shouldConvertTonnesToKilograms() {
        BigDecimal result = converter.toKilograms(
                new BigDecimal("2.500"),
                CropUnit.TON
        );

        assertEquals(new BigDecimal("2500.000"), result);
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> converter.toKilograms(
                        BigDecimal.ZERO,
                        CropUnit.KG
                )
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> converter.toKilograms(
                        new BigDecimal("-10"),
                        CropUnit.KG
                )
        );
    }

    @Test
    void shouldRejectNullQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> converter.toKilograms(null, CropUnit.KG)
        );
    }

    @Test
    void shouldRejectNullUnit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> converter.toKilograms(
                        new BigDecimal("10"),
                        null
                )
        );
    }
}