package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.BadRequestException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.MandiPriceTrendResponse;
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
public class MandiPriceTrendService {

    private static final int SCALE = 2;
    private static final BigDecimal STABLE_THRESHOLD =
            new BigDecimal("1.00");

    private final MandiPriceObservationRepository repository;

    public MandiPriceTrendService(
            MandiPriceObservationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public MandiPriceTrendResponse analyzeTrend(
            String cropName,
            String marketName,
            String state) {

        validateInput(cropName, marketName, state);

        LocalDate today = LocalDate.now();

        List<MandiPriceObservation> observations =
                repository
                        .findByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseAndObservationDateBetweenOrderByObservationDateAsc(
                                cropName.trim(),
                                marketName.trim(),
                                state.trim(),
                                today.minusDays(29),
                                today
                        );

        List<MandiPriceObservation> daily =
                latestObservationPerDay(observations);

        if (daily.isEmpty()) {
            throw new BadRequestException(
                    "No price observations found for the requested "
                            + "crop, market and state in the last 30 days");
        }

        MandiPriceObservation latest =
                daily.get(daily.size() - 1);

        List<BigDecimal> prices = daily.stream()
                .map(MandiPriceObservation::getModalPricePerQuintal)
                .toList();

        BigDecimal latestPrice = latest.getModalPricePerQuintal();

        LocalDate sevenDayStart = today.minusDays(6);
        LocalDate thirtyDayStart = today.minusDays(29);

        List<BigDecimal> prices7Days = pricesInRange(
                daily, sevenDayStart, today);

        List<BigDecimal> prices30Days = pricesInRange(
                daily, thirtyDayStart, today);

        BigDecimal average7Days = average(prices7Days);
        BigDecimal average30Days = average(prices30Days);

        BigDecimal baseline7Days = baselineBefore(
                daily, sevenDayStart);

        BigDecimal baseline30Days = baselineBefore(
                daily, thirtyDayStart);

        BigDecimal change7Days = change(
                average7Days, baseline7Days);

        BigDecimal change30Days = change(
                average30Days, baseline30Days);

        BigDecimal percentage7Days = percentageChange(
                average7Days, baseline7Days);

        BigDecimal percentage30Days = percentageChange(
                average30Days, baseline30Days);

        BigDecimal minimum = prices.stream()
                .min(BigDecimal::compareTo)
                .orElseThrow();

        BigDecimal maximum = prices.stream()
                .max(BigDecimal::compareTo)
                .orElseThrow();

        BigDecimal historicalAverage = average(prices);

        String trend = classifyTrend(percentage7Days);
        String message = buildMessage(
                trend, percentage7Days, percentage30Days);

        return new MandiPriceTrendResponse(
                latest.getCropName(),
                latest.getMarketName(),
                latest.getState(),
                latest.getObservationDate(),
                round(latestPrice),
                average7Days,
                change7Days,
                percentage7Days,
                average30Days,
                change30Days,
                percentage30Days,
                round(minimum),
                round(maximum),
                historicalAverage,
                daily.size(),
                daily.get(0).getObservationDate(),
                latest.getObservationDate(),
                trend,
                message
        );
    }

    private List<MandiPriceObservation> latestObservationPerDay(
            List<MandiPriceObservation> observations) {

        List<MandiPriceObservation> daily = new ArrayList<>();

        for (MandiPriceObservation observation : observations) {
            int lastIndex = daily.size() - 1;

            if (lastIndex >= 0
                    && daily.get(lastIndex).getObservationDate()
                    .equals(observation.getObservationDate())) {
                daily.set(lastIndex, observation);
            } else {
                daily.add(observation);
            }
        }

        return daily;
    }

    private List<BigDecimal> pricesInRange(
            List<MandiPriceObservation> observations,
            LocalDate from,
            LocalDate to) {

        return observations.stream()
                .filter(o -> !o.getObservationDate().isBefore(from)
                        && !o.getObservationDate().isAfter(to))
                .map(MandiPriceObservation::getModalPricePerQuintal)
                .toList();
    }

    private BigDecimal baselineBefore(
            List<MandiPriceObservation> observations,
            LocalDate periodStart) {

        return observations.stream()
                .filter(o -> o.getObservationDate().isBefore(periodStart))
                .reduce((first, second) -> second)
                .map(MandiPriceObservation::getModalPricePerQuintal)
                .orElse(null);
    }

    private BigDecimal average(List<BigDecimal> prices) {
        if (prices.isEmpty()) {
            return null;
        }

        return prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(
                        BigDecimal.valueOf(prices.size()),
                        SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal change(
            BigDecimal current,
            BigDecimal baseline) {

        if (current == null || baseline == null) {
            return null;
        }

        return round(current.subtract(baseline));
    }

    private BigDecimal percentageChange(
            BigDecimal current,
            BigDecimal baseline) {

        if (current == null || baseline == null
                || baseline.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return current.subtract(baseline)
                .multiply(BigDecimal.valueOf(100))
                .divide(baseline, SCALE, RoundingMode.HALF_UP);
    }

    private String classifyTrend(BigDecimal percentageChange) {
        if (percentageChange == null) {
            return "INSUFFICIENT_DATA";
        }

        if (percentageChange.compareTo(STABLE_THRESHOLD) > 0) {
            return "RISING";
        }

        if (percentageChange.compareTo(
                STABLE_THRESHOLD.negate()) < 0) {
            return "FALLING";
        }

        return "STABLE";
    }

    private String buildMessage(
            String trend,
            BigDecimal change7Days,
            BigDecimal change30Days) {

        if ("INSUFFICIENT_DATA".equals(trend)) {
            return "Insufficient historical baseline data to classify "
                    + "the 7-day price trend.";
        }

        return "The 7-day trend is " + trend
                + ". 7-day percentage change: " + change7Days
                + "%. 30-day percentage change: "
                + (change30Days == null
                ? "unavailable" : change30Days + "%")
                + ". This describes historical prices and is not a forecast.";
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private void validateInput(
            String cropName,
            String marketName,
            String state) {

        if (cropName == null || cropName.isBlank()
                || marketName == null || marketName.isBlank()
                || state == null || state.isBlank()) {
            throw new BadRequestException(
                    "Crop name, market name and state are required");
        }
    }
}
