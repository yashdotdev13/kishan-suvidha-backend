package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement;

import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.BadRequestException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.MandiPriceForecastResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.MandiPriceObservation;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository.MandiPriceObservationRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.MandiPriceForecastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MandiPriceForecastServiceTest {

    @Mock
    private MandiPriceObservationRepository repository;

    private MandiPriceForecastService forecastService;

    @BeforeEach
    void setUp() {
        forecastService = new MandiPriceForecastService(repository);
    }

    @Test
    void shouldForecastUsingLatestSevenDistinctObservations() {
        LocalDate today = LocalDate.now();

        when(repository
                .findByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseAndObservationDateBetweenOrderByObservationDateAsc(
                        "Wheat", "Test Mandi", "Punjab",
                        today.minusYears(2), today
                ))
                .thenReturn(List.of(
                        observation(1L, today.minusDays(7), "2300"),
                        observation(2L, today.minusDays(6), "2310"),
                        observation(3L, today.minusDays(5), "2320"),
                        observation(4L, today.minusDays(4), "2330"),
                        observation(5L, today.minusDays(3), "2340"),
                        observation(6L, today.minusDays(2), "2350"),
                        observation(7L, today.minusDays(1), "2360")
                ));

        MandiPriceForecastResponse result =
                forecastService.forecastNextDay(
                        "Wheat", "Test Mandi", "Punjab"
                );

        assertEquals("Wheat", result.cropName());
        assertEquals("Test Mandi", result.marketName());
        assertEquals("Punjab", result.state());
        assertEquals(today.plusDays(1), result.forecastDate());
        assertEquals(7, result.trainingObservationCount());
        assertEquals(
                0,
                new BigDecimal("2330.00").compareTo(
                        result.predictedModalPricePerQuintal()
                )
        );
        assertTrue(
                result.lowerPredictionBound()
                        .compareTo(result.upperPredictionBound()) <= 0
        );
        assertFalse(result.limitations().isEmpty());
    }

    @Test
    void shouldRejectForecastWhenFewerThanSevenObservationsExist() {
        LocalDate today = LocalDate.now();

        when(repository
                .findByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseAndObservationDateBetweenOrderByObservationDateAsc(
                        "Wheat", "Test Mandi", "Punjab",
                        today.minusYears(2), today
                ))
                .thenReturn(List.of(
                        observation(1L, today.minusDays(1), "2400")
                ));

        assertThrows(
                BadRequestException.class,
                () -> forecastService.forecastNextDay(
                        "Wheat", "Test Mandi", "Punjab"
                )
        );
    }

    @Test
    void shouldRejectBlankCropName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> forecastService.forecastNextDay(
                        " ", "Test Mandi", "Punjab"
                )
        );

        verifyNoInteractions(repository);
    }

    private MandiPriceObservation observation(
            Long id,
            LocalDate date,
            String price
    ) {
        return MandiPriceObservation.builder()
                .id(id)
                .cropName("Wheat")
                .marketName("Test Mandi")
                .state("Punjab")
                .observationDate(date)
                .modalPricePerQuintal(new BigDecimal(price))
                .source("MANUAL_TEST")
                .sourceRecordId("TEST-" + id)
                .build();
    }
}