package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement;

import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.BadRequestException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.MandiPriceBacktestResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.MandiPriceObservation;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository.MandiPriceObservationRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.MandiPriceBacktestService;
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
class MandiPriceBacktestServiceTest {

    @Mock
    private MandiPriceObservationRepository repository;

    private MandiPriceBacktestService backtestService;

    @BeforeEach
    void setUp() {
        backtestService = new MandiPriceBacktestService(repository);
    }

    @Test
    void shouldCalculateBacktestMetrics() {
        LocalDate evaluationDate = LocalDate.now().minusDays(1);
        LocalDate queryStart = evaluationDate.minusDays(7);

        when(repository
                .findByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseAndObservationDateBetweenOrderByObservationDateAsc(
                        "Wheat",
                        "Test Mandi",
                        "Punjab",
                        queryStart,
                        evaluationDate
                ))
                .thenReturn(List.of(
                        observation(1L, queryStart, "100"),
                        observation(2L, queryStart.plusDays(1), "100"),
                        observation(3L, queryStart.plusDays(2), "100"),
                        observation(4L, queryStart.plusDays(3), "100"),
                        observation(5L, queryStart.plusDays(4), "100"),
                        observation(6L, queryStart.plusDays(5), "100"),
                        observation(7L, queryStart.plusDays(6), "100"),
                        observation(8L, evaluationDate, "110")
                ));

        MandiPriceBacktestResponse result = backtestService.backtest(
                "Wheat",
                "Test Mandi",
                "Punjab",
                evaluationDate,
                evaluationDate
        );

        assertEquals("Wheat", result.cropName());
        assertEquals("Test Mandi", result.marketName());
        assertEquals("Punjab", result.state());
        assertEquals(1, result.evaluationCount());

        assertEquals(
                0,
                new BigDecimal("10.00").compareTo(result.meanAbsoluteError())
        );

        assertEquals(
                0,
                new BigDecimal("9.09").compareTo(
                        result.meanAbsolutePercentageError()
                )
        );

        assertEquals(
                0,
                new BigDecimal("10.00").compareTo(
                        result.rootMeanSquaredError()
                )
        );

        assertFalse(result.limitations().isEmpty());
    }

    @Test
    void shouldRejectBacktestWhenNoForecastCanBeEvaluated() {
        LocalDate evaluationDate = LocalDate.now().minusDays(1);

        when(repository
                .findByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseAndObservationDateBetweenOrderByObservationDateAsc(
                        "Wheat",
                        "Test Mandi",
                        "Punjab",
                        evaluationDate.minusDays(7),
                        evaluationDate
                ))
                .thenReturn(List.of());

        assertThrows(
                BadRequestException.class,
                () -> backtestService.backtest(
                        "Wheat",
                        "Test Mandi",
                        "Punjab",
                        evaluationDate,
                        evaluationDate
                )
        );
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
                .sourceRecordId("BACKTEST-" + id)
                .build();
    }
}