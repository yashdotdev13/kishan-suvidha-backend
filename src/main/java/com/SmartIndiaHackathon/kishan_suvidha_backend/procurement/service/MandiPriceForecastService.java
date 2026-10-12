package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.MandiPriceForecastResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.MandiPriceObservation;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository.MandiPriceObservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MandiPriceForecastService {

    private static final int WINDOW_SIZE = 7;
    private static final int MINIMUM_OBSERVATIONS = 7;
    private static final BigDecimal Z_SCORE = new BigDecimal("1.96");

    private final MandiPriceObservationRepository repository;

    public MandiPriceForecastService(
            MandiPriceObservationRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public MandiPriceForecastResponse forecastNextDay(
            String cropName,
            String marketName,
            String state
    ) {
        validateInput(cropName, marketName, state);

        LocalDate today = LocalDate.now();

        List<MandiPriceObservation> observations =
                repository
                        .findByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseAndObservationDateBetweenOrderByObservationDateAsc(
                                cropName.trim(),
                                marketName.trim(),
                                state.trim(),
                                today.minusYears(2),
                                today
                        );

        // Keep the latest observation for each date to avoid counting
        // multiple records from the same day as independent daily data.
        List<MandiPriceObservation> dailyObservations =
                new ArrayList<>();

        for (MandiPriceObservation observation : observations) {
            int lastIndex = dailyObservations.size() - 1;

            if (lastIndex >= 0
                    && dailyObservations.get(lastIndex)
                    .getObservationDate()
                    .equals(observation.getObservationDate())) {
                dailyObservations.set(lastIndex, observation);
            } else {
                dailyObservations.add(observation);
            }
        }

        if (dailyObservations.size() < MINIMUM_OBSERVATIONS) {
            throw new com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.BadRequestException(
                    "At least 7 distinct daily price observations are required "
                            + "for a baseline forecast. Found: "
                            + dailyObservations.size()
            );
        }

        int start = dailyObservations.size() - WINDOW_SIZE;

        List<BigDecimal> prices = dailyObservations.subList(
                        start, dailyObservations.size()
                )
                .stream()
                .map(MandiPriceObservation::getModalPricePerQuintal)
                .toList();

        BigDecimal mean = prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(
                        BigDecimal.valueOf(prices.size()),
                        4,
                        RoundingMode.HALF_UP
                );

        BigDecimal variance = prices.stream()
                .map(price -> price.subtract(mean))
                .map(diff -> diff.multiply(diff))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(
                        BigDecimal.valueOf(prices.size()),
                        8,
                        RoundingMode.HALF_UP
                );

        BigDecimal standardDeviation = BigDecimal.valueOf(
                Math.sqrt(variance.doubleValue())
        );

        // Approximate interval for the estimated mean under
        // independent, normally distributed observations.
        // This is not a validated prediction interval.
        BigDecimal margin = Z_SCORE
                .multiply(standardDeviation)
                .divide(
                        BigDecimal.valueOf(Math.sqrt(prices.size())),
                        4,
                        RoundingMode.HALF_UP
                );

        BigDecimal lower = mean.subtract(margin)
                .max(BigDecimal.ZERO);

        BigDecimal upper = mean.add(margin);

        return new MandiPriceForecastResponse(
                dailyObservations.get(0).getCropName(),
                dailyObservations.get(0).getMarketName(),
                dailyObservations.get(0).getState(),
                today.plusDays(1),
                mean.setScale(2, RoundingMode.HALF_UP),
                lower.setScale(2, RoundingMode.HALF_UP),
                upper.setScale(2, RoundingMode.HALF_UP),
                "SEVEN_OBSERVATION_MOVING_AVERAGE_BASELINE",
                prices.size(),
                "Approximate 95% confidence interval for the recent mean; "
                        + "not a validated next-day prediction interval.",
                List.of(
                        "Requires at least 7 distinct daily observations.",
                        "Missing dates are not imputed.",
                        "The baseline does not model seasonality, trends, "
                                + "weather or arrivals.",
                        "Backtesting is required before using this forecast "
                                + "for sale-timing decisions."
                )
        );
    }

    private void validateInput(
            String cropName,
            String marketName,
            String state
    ) {
        if (cropName == null || cropName.isBlank()
                || marketName == null || marketName.isBlank()
                || state == null || state.isBlank()) {
            throw new IllegalArgumentException(
                    "Crop name, market name and state are required"
            );
        }
    }
}