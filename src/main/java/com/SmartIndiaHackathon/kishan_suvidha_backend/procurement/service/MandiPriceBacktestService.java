package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.MandiPriceBacktestResponse;
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
public class MandiPriceBacktestService {

    private static final int WINDOW_SIZE = 7;
    private static final int SCALE = 8;

    private final MandiPriceObservationRepository repository;

    public MandiPriceBacktestService(
            MandiPriceObservationRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public MandiPriceBacktestResponse backtest(
            String cropName,
            String marketName,
            String state,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        validateInputs(
                cropName, marketName, state, fromDate, toDate
        );

        List<MandiPriceObservation> observations =
                repository
                        .findByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseAndObservationDateBetweenOrderByObservationDateAsc(
                                cropName.trim(),
                                marketName.trim(),
                                state.trim(),
                                fromDate.minusDays(WINDOW_SIZE),
                                toDate
                        );

        List<MandiPriceObservation> daily =
                latestObservationPerDay(observations);

        List<BigDecimal> absoluteErrors = new ArrayList<>();
        List<BigDecimal> squaredErrors = new ArrayList<>();
        List<BigDecimal> percentageErrors = new ArrayList<>();

        int evaluationCount = 0;

        for (int i = 0; i < daily.size(); i++) {
            MandiPriceObservation actualObservation = daily.get(i);
            LocalDate actualDate = actualObservation.getObservationDate();

            if (actualDate.isBefore(fromDate)
                    || actualDate.isAfter(toDate)) {
                continue;
            }

            // Use only the preceding 7 observations. Never include
            // the actual day's price in its own forecast.
            if (i < WINDOW_SIZE) {
                continue;
            }

            BigDecimal predicted = daily.subList(
                            i - WINDOW_SIZE, i
                    )
                    .stream()
                    .map(MandiPriceObservation::getModalPricePerQuintal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(
                            BigDecimal.valueOf(WINDOW_SIZE),
                            SCALE,
                            RoundingMode.HALF_UP
                    );

            BigDecimal actual =
                    actualObservation.getModalPricePerQuintal();

            BigDecimal absoluteError =
                    actual.subtract(predicted).abs();

            absoluteErrors.add(absoluteError);
            squaredErrors.add(
                    absoluteError.multiply(absoluteError)
            );

            if (actual.compareTo(BigDecimal.ZERO) > 0) {
                percentageErrors.add(
                        absoluteError.multiply(BigDecimal.valueOf(100))
                                .divide(actual, SCALE, RoundingMode.HALF_UP)
                );
            }

            evaluationCount++;
        }

        if (evaluationCount == 0) {
            throw new com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.BadRequestException(
                    "Not enough historical observations to backtest. "
                            + "Provide at least 8 distinct daily observations "
                            + "with a suitable evaluation date range."
            );
        }

        BigDecimal mae = average(absoluteErrors);

        BigDecimal rmse = BigDecimal.valueOf(
                Math.sqrt(average(squaredErrors).doubleValue())
        );

        BigDecimal mape = percentageErrors.isEmpty()
                ? null
                : average(percentageErrors);

        return new MandiPriceBacktestResponse(
                cropName.trim(),
                marketName.trim(),
                state.trim(),
                fromDate,
                toDate,
                evaluationCount,
                mae.setScale(2, RoundingMode.HALF_UP),
                mape == null
                        ? null
                        : mape.setScale(2, RoundingMode.HALF_UP),
                rmse.setScale(2, RoundingMode.HALF_UP),
                "SEVEN_OBSERVATION_ROLLING_MEAN",
                List.of(
                        "This is a baseline evaluation, not evidence "
                                + "of future forecast accuracy.",
                        "Only recorded observations are used; missing dates "
                                + "are not imputed.",
                        "Multiple records for one date are reduced to the "
                                + "latest record returned by the repository.",
                        "MAPE excludes observations with non-positive "
                                + "actual prices.",
                        "Results may be unreliable when the historical "
                                + "sample is small or unrepresentative."
                )
        );
    }

    private List<MandiPriceObservation> latestObservationPerDay(
            List<MandiPriceObservation> observations
    ) {
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

    private BigDecimal average(List<BigDecimal> values) {
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(
                        BigDecimal.valueOf(values.size()),
                        SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private void validateInputs(
            String cropName,
            String marketName,
            String state,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        if (cropName == null || cropName.isBlank()
                || marketName == null || marketName.isBlank()
                || state == null || state.isBlank()) {
            throw new IllegalArgumentException(
                    "Crop name, market name and state are required"
            );
        }

        if (fromDate == null || toDate == null
                || fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "A valid date range is required"
            );
        }

        if (toDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Backtest end date cannot be in the future"
            );
        }
    }
}