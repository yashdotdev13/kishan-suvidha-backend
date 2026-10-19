package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.MandiPriceTrendResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.MandiPriceObservation;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository.MandiPriceObservationRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.MandiPriceTrendService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MandiPriceTrendServiceTest {

    @Mock
    private MandiPriceObservationRepository repository;

    @InjectMocks
    private MandiPriceTrendService service;

    private MandiPriceObservation observation(
            LocalDate date, String price) {
        return MandiPriceObservation.builder()
                .id((long) date.toEpochDay())
                .cropName("Wheat")
                .marketName("Patna Mandi")
                .state("Bihar")
                .observationDate(date)
                .modalPricePerQuintal(new BigDecimal(price))
                .source("TEST")
                .sourceRecordId("record-" + date)
                .build();
    }

    private List<MandiPriceObservation> observationsWithBaseline(
            String baselinePrice, String recentPrice) {
        LocalDate today = LocalDate.now();
        List<MandiPriceObservation> observations = new ArrayList<>();

        observations.add(observation(
                today.minusDays(7), baselinePrice));

        for (int day = 6; day >= 0; day--) {
            observations.add(observation(
                    today.minusDays(day), recentPrice));
        }

        return observations;
    }

    private void mockObservations(
            List<MandiPriceObservation> observations) {
        when(repository
                .findByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseAndObservationDateBetweenOrderByObservationDateAsc(
                        eq("Wheat"),
                        eq("Patna Mandi"),
                        eq("Bihar"),
                        any(LocalDate.class),
                        any(LocalDate.class)))
                .thenReturn(observations);
    }

    @Test
    void shouldClassifyRisingTrend() {
        mockObservations(observationsWithBaseline("100", "110"));

        MandiPriceTrendResponse response =
                service.analyzeTrend("Wheat", "Patna Mandi", "Bihar");

        assertEquals("RISING", response.trend());
        assertEquals(new BigDecimal("110.00"),
                response.latestModalPricePerQuintal());
        assertEquals(new BigDecimal("10.00"),
                response.priceChangePercentage7Days());
    }

    @Test
    void shouldClassifyFallingTrend() {
        mockObservations(observationsWithBaseline("100", "90"));

        MandiPriceTrendResponse response =
                service.analyzeTrend("Wheat", "Patna Mandi", "Bihar");

        assertEquals("FALLING", response.trend());
        assertEquals(new BigDecimal("-10.00"),
                response.priceChangePercentage7Days());
    }

    @Test
    void shouldClassifyStableTrend() {
        mockObservations(observationsWithBaseline("100", "100"));

        MandiPriceTrendResponse response =
                service.analyzeTrend("Wheat", "Patna Mandi", "Bihar");

        assertEquals("STABLE", response.trend());
        assertEquals(new BigDecimal("0.00"),
                response.priceChangePercentage7Days());
    }

    @Test
    void shouldReturnInsufficientDataWithoutEarlierBaseline() {
        LocalDate today = LocalDate.now();

        mockObservations(List.of(
                observation(today.minusDays(2), "100"),
                observation(today, "110")
        ));

        MandiPriceTrendResponse response =
                service.analyzeTrend("Wheat", "Patna Mandi", "Bihar");

        assertEquals("INSUFFICIENT_DATA", response.trend());
        assertNull(response.priceChangePercentage7Days());
    }

    @Test
    void shouldRejectBlankInput() {
        assertThrows(
                RuntimeException.class,
                () -> service.analyzeTrend("", "Patna Mandi", "Bihar")
        );

        verifyNoInteractions(repository);
    }
}
