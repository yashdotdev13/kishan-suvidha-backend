package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.BadRequestException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.MandiPriceForecastResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.SaleTimingRecommendationResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.SaleTimingRecommendationResponse.Recommendation;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.MandiPriceObservation;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository.MandiPriceObservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SaleTimingRecommendationService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int PRICE_SCALE = 2;
    private static final int PERCENTAGE_SCALE = 2;

    private final MandiPriceObservationRepository repository;
    private final MandiPriceForecastService forecastService;
    private final BigDecimal waitThresholdPercentage;
    private final BigDecimal sellThresholdPercentage;
    private final long maximumPriceAgeDays;

    public SaleTimingRecommendationService(
            MandiPriceObservationRepository repository,
            MandiPriceForecastService forecastService,
            @Value("${kishan.suvidha.sale-timing.wait-threshold-percentage:2.0}")
            BigDecimal waitThresholdPercentage,
            @Value("${kishan.suvidha.sale-timing.sell-threshold-percentage:2.0}")
            BigDecimal sellThresholdPercentage,
            @Value("${kishan.suvidha.sale-timing.maximum-price-age-days:3}")
            long maximumPriceAgeDays
    ) {
        if (waitThresholdPercentage.signum() < 0
                || sellThresholdPercentage.signum() < 0
                || maximumPriceAgeDays < 0) {
            throw new IllegalArgumentException(
                    "Sale-timing thresholds and maximum price age "
                            + "must not be negative"
            );
        }

        this.repository = repository;
        this.forecastService = forecastService;
        this.waitThresholdPercentage = waitThresholdPercentage;
        this.sellThresholdPercentage = sellThresholdPercentage;
        this.maximumPriceAgeDays = maximumPriceAgeDays;
    }

    @Transactional(readOnly = true)
    public SaleTimingRecommendationResponse recommend(
            String cropName,
            String marketName,
            String state
    ) {
        validateInput(cropName, marketName, state);

        LocalDate today = LocalDate.now();

        Optional<MandiPriceObservation> latestOptional =
                repository
                        .findFirstByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseOrderByObservationDateDescIdDesc(
                                cropName.trim(),
                                marketName.trim(),
                                state.trim()
                        );

        if (latestOptional.isEmpty()) {
            return insufficientData(
                    cropName.trim(),
                    marketName.trim(),
                    state.trim(),
                    "No historical mandi price observations are available."
            );
        }

        MandiPriceObservation latest = latestOptional.get();

        if (latest.getObservationDate() == null
                || latest.getModalPricePerQuintal() == null
                || latest.getModalPricePerQuintal().signum() <= 0) {
            return insufficientData(
                    latest.getCropName(),
                    latest.getMarketName(),
                    latest.getState(),
                    "The latest observation has an invalid date or modal price."
            );
        }

        long priceAgeDays = java.time.temporal.ChronoUnit.DAYS.between(
                latest.getObservationDate(), today
        );

        if (priceAgeDays < 0 || priceAgeDays > maximumPriceAgeDays) {
            return insufficientData(
                    latest.getCropName(),
                    latest.getMarketName(),
                    latest.getState(),
                    "The latest price is stale or dated in the future."
            );
        }

        MandiPriceForecastResponse forecast;

        try {
            forecast = forecastService.forecastNextDay(
                    latest.getCropName(),
                    latest.getMarketName(),
                    latest.getState()
            );
        } catch (BadRequestException exception) {
            return insufficientData(
                    latest.getCropName(),
                    latest.getMarketName(),
                    latest.getState(),
                    "Insufficient historical observations to generate a forecast."
            );
        }

        BigDecimal currentPrice = latest.getModalPricePerQuintal();
        BigDecimal predictedPrice =
                forecast.predictedModalPricePerQuintal();

        if (predictedPrice == null || predictedPrice.signum() <= 0) {
            return insufficientData(
                    latest.getCropName(),
                    latest.getMarketName(),
                    latest.getState(),
                    "The forecast price is invalid."
            );
        }

        BigDecimal percentageChange = predictedPrice
                .subtract(currentPrice)
                .multiply(HUNDRED)
                .divide(currentPrice, 4, RoundingMode.HALF_UP)
                .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);

        Recommendation recommendation;
        String rationale;

        if (percentageChange.compareTo(waitThresholdPercentage) >= 0) {
            recommendation = Recommendation.WAIT_AND_MONITOR;
            rationale = "The baseline forecast is above the current price "
                    + "by at least the configured wait threshold. "
                    + "Monitor updated mandi prices before deciding.";
        } else if (percentageChange.compareTo(
                sellThresholdPercentage.negate()) <= 0) {
            recommendation = Recommendation.SELL_NOW;
            rationale = "The baseline forecast is below the current price "
                    + "by at least the configured sell threshold. "
                    + "Consider current market conditions and your own costs.";
        } else {
            recommendation = Recommendation.WAIT_AND_MONITOR;
            rationale = "The estimated price movement is within the configured "
                    + "thresholds. Monitor updated prices rather than relying "
                    + "on a strong sell or wait signal.";
        }

        List<String> limitations = new ArrayList<>(forecast.limitations());
        limitations.add(
                "This rule-based recommendation is not financial advice "
                        + "and does not guarantee a better sale price."
        );
        limitations.add(
                "Storage costs, spoilage risk, transport costs, quality, "
                        + "and minimum support price are not considered."
        );

        return new SaleTimingRecommendationResponse(
                latest.getCropName(),
                latest.getMarketName(),
                latest.getState(),
                currentPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP),
                latest.getObservationDate(),
                predictedPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP),
                forecast.forecastDate(),
                percentageChange,
                recommendation,
                rationale,
                forecast.uncertaintyDescription(),
                List.copyOf(limitations)
        );
    }

    private SaleTimingRecommendationResponse insufficientData(
            String cropName,
            String marketName,
            String state,
            String reason
    ) {
        return new SaleTimingRecommendationResponse(
                cropName,
                marketName,
                state,
                null,
                null,
                null,
                null,
                null,
                Recommendation.INSUFFICIENT_DATA,
                reason,
                "A reliable recommendation cannot be generated from "
                        + "the available observations.",
                List.of(
                        "Verify the latest mandi price and its observation date.",
                        "At least seven distinct daily observations are required "
                                + "for the current baseline forecast.",
                        "A forecast is an estimate, not a guaranteed market price."
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