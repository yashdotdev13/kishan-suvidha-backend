package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement;

import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.BadRequestException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.MandiPriceForecastResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.SaleTimingRecommendationResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.MandiPriceObservation;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository.MandiPriceObservationRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.MandiPriceForecastService;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.SaleTimingRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleTimingRecommendationServiceTest {

    @Mock
    private MandiPriceObservationRepository repository;

    @Mock
    private MandiPriceForecastService forecastService;

    private SaleTimingRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new SaleTimingRecommendationService(
                repository,
                forecastService,
                new BigDecimal("2.0"),
                new BigDecimal("2.0"),
                3
        );
    }

    @Test
    void shouldRecommendWaitWhenForecastRisesByAtLeastTwoPercent() {
        LocalDate today = LocalDate.now();

        mockLatestObservation(today, "2400");
        mockForecast("2450");

        SaleTimingRecommendationResponse result =
                service.recommend("Wheat", "Test Mandi", "Punjab");

        assertEquals(
                SaleTimingRecommendationResponse.Recommendation.WAIT_AND_MONITOR,
                result.recommendation()
        );
        assertEquals(new BigDecimal("2.08"), result.expectedChangePercentage());
        assertNotNull(result.rationale());
    }

    @Test
    void shouldRecommendSellWhenForecastFallsByAtLeastTwoPercent() {
        LocalDate today = LocalDate.now();

        mockLatestObservation(today, "2400");
        mockForecast("2300");

        SaleTimingRecommendationResponse result =
                service.recommend("Wheat", "Test Mandi", "Punjab");

        assertEquals(
                SaleTimingRecommendationResponse.Recommendation.SELL_NOW,
                result.recommendation()
        );
        assertEquals(new BigDecimal("-4.17"), result.expectedChangePercentage());
    }

    @Test
    void shouldReturnInsufficientDataWhenLatestPriceIsStale() {
        LocalDate staleDate = LocalDate.now().minusDays(4);

        mockLatestObservation(staleDate, "2400");

        SaleTimingRecommendationResponse result =
                service.recommend("Wheat", "Test Mandi", "Punjab");

        assertEquals(
                SaleTimingRecommendationResponse.Recommendation.INSUFFICIENT_DATA,
                result.recommendation()
        );
        verifyNoInteractions(forecastService);
    }

    @Test
    void shouldReturnInsufficientDataWhenNoObservationExists() {
        when(repository
                .findFirstByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseOrderByObservationDateDescIdDesc(
                        "Wheat", "Test Mandi", "Punjab"
                ))
                .thenReturn(Optional.empty());

        SaleTimingRecommendationResponse result =
                service.recommend("Wheat", "Test Mandi", "Punjab");

        assertEquals(
                SaleTimingRecommendationResponse.Recommendation.INSUFFICIENT_DATA,
                result.recommendation()
        );
        assertNull(result.currentModalPricePerQuintal());
    }

    @Test
    void shouldReturnInsufficientDataWhenForecastHasTooFewObservations() {
        LocalDate today = LocalDate.now();

        mockLatestObservation(today, "2400");

        when(forecastService.forecastNextDay(
                "Wheat", "Test Mandi", "Punjab"
        )).thenThrow(new BadRequestException("Insufficient observations"));

        SaleTimingRecommendationResponse result =
                service.recommend("Wheat", "Test Mandi", "Punjab");

        assertEquals(
                SaleTimingRecommendationResponse.Recommendation.INSUFFICIENT_DATA,
                result.recommendation()
        );
    }

    @Test
    void shouldRejectBlankCropName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.recommend(" ", "Test Mandi", "Punjab")
        );

        verifyNoInteractions(repository, forecastService);
    }

    private void mockLatestObservation(LocalDate date, String price) {
        MandiPriceObservation observation = MandiPriceObservation.builder()
                .id(1L)
                .cropName("Wheat")
                .marketName("Test Mandi")
                .state("Punjab")
                .observationDate(date)
                .modalPricePerQuintal(new BigDecimal(price))
                .source("MANUAL_TEST")
                .sourceRecordId("SALE-TEST-1")
                .build();

        when(repository
                .findFirstByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseOrderByObservationDateDescIdDesc(
                        "Wheat", "Test Mandi", "Punjab"
                ))
                .thenReturn(Optional.of(observation));
    }

    private void mockForecast(String predictedPrice) {
        MandiPriceForecastResponse forecast =
                new MandiPriceForecastResponse(
                        "Wheat",
                        "Test Mandi",
                        "Punjab",
                        LocalDate.now().plusDays(1),
                        new BigDecimal(predictedPrice),
                        new BigDecimal("2200"),
                        new BigDecimal("2600"),
                        "SEVEN_OBSERVATION_MOVING_AVERAGE_BASELINE",
                        7,
                        "Approximate uncertainty; not a validated prediction interval.",
                        List.of("Baseline forecast limitations apply.")
                );

        when(forecastService.forecastNextDay(
                "Wheat", "Test Mandi", "Punjab"
        )).thenReturn(forecast);
    }
}
