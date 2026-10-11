package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity.Crop;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.repository.CropRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationContribution;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationDemand;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationPlan;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationContributionStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationDemandStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationPlanStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationContributionRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationDemandRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AggregationMatchingService {

    private static final int CANDIDATE_LIMIT = 500;
    private static final String ALGORITHM_VERSION = "v1";

    private final CropRepository cropRepository;
    private final AggregationDemandRepository demandRepository;
    private final AggregationPlanRepository planRepository;
    private final AggregationContributionRepository contributionRepository;
    private final CropQuantityConverter quantityConverter;

    @Transactional
    public AggregationPlan generatePlan(Long demandId) {
        AggregationDemand demand = demandRepository.findById(demandId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Aggregation demand not found"));

        if (demand.getStatus() != AggregationDemandStatus.OPEN
                && demand.getStatus() != AggregationDemandStatus.PLAN_PROPOSED) {
            throw new IllegalStateException(
                    "A plan cannot be generated for a demand in status "
                            + demand.getStatus());
        }

        List<Crop> candidates = cropRepository.findByCropTypeAndStatus(
                demand.getCropType(),
                CropStatus.AVAILABLE,
                PageRequest.of(0, CANDIDATE_LIMIT)
        );

        List<Crop> matchingCrops = candidates.stream()
                .filter(crop -> crop.getCropName() != null
                        && crop.getCropName().trim()
                        .equalsIgnoreCase(demand.getCropName().trim()))
                .filter(crop -> crop.getQuantity() != null
                        && crop.getQuantity().signum() > 0)
                .filter(crop -> crop.getUnit() != null)
                .filter(crop -> crop.getExpectedPrice() != null
                        && crop.getExpectedPrice().signum() >= 0)
                .filter(crop -> demand.getMaxPricePerKg() == null
                        || crop.getExpectedPrice().compareTo(
                        demand.getMaxPricePerKg()) <= 0)
                .sorted(Comparator
                        .comparingDouble((Crop crop) -> distanceFromDemand(crop, demand))
                        .thenComparing(Crop::getId))
                .toList();

        BigDecimal remainingKg = demand.getTargetQuantityKg();
        BigDecimal plannedKg = BigDecimal.ZERO;
        BigDecimal estimatedTotalCost = BigDecimal.ZERO;
        BigDecimal estimatedDistanceSum = BigDecimal.ZERO;
        List<AggregationContribution> contributions = new ArrayList<>();

        for (Crop crop : matchingCrops) {
            if (remainingKg.signum() <= 0) {
                break;
            }

            BigDecimal availableKg = quantityConverter.toKilograms(
                    crop.getQuantity(), crop.getUnit());

            BigDecimal contributionKg = availableKg.min(remainingKg);

            AggregationContribution contribution =
                    AggregationContribution.builder()
                            .plan(null)
                            .crop(crop)
                            .proposedQuantityKg(contributionKg)
                            .reservedQuantityKg(BigDecimal.ZERO)
                            .status(AggregationContributionStatus.PROPOSED)
                            .build();

            contributions.add(contribution);

            plannedKg = plannedKg.add(contributionKg);
            remainingKg = remainingKg.subtract(contributionKg);

            /*
             * NOTE:
             * Crop.expectedPrice unit semantics must be confirmed before
             * using it for reliable cost calculations or price filtering.
             * This initial version records no invented monetary estimate.
             */
            double distance = distanceFromDemand(crop, demand);
            if (Double.isFinite(distance)) {
                estimatedDistanceSum = estimatedDistanceSum.add(
                        BigDecimal.valueOf(distance));
            }
        }

        if (contributions.isEmpty()) {
            throw new IllegalStateException(
                    "No compatible available crops were found for this demand");
        }

        AggregationPlan plan = AggregationPlan.builder()
                .demand(demand)
                .plannedQuantityKg(plannedKg)
                .estimatedTotalCost(estimatedTotalCost)
                .estimatedTransportCost(BigDecimal.ZERO)
                .estimatedDistanceKm(
                        estimatedDistanceSum.setScale(2, RoundingMode.HALF_UP))
                .algorithmVersion(ALGORITHM_VERSION)
                .status(AggregationPlanStatus.PROPOSED)
                .build();

        AggregationPlan savedPlan = planRepository.save(plan);

        contributions.forEach(contribution -> contribution.setPlan(savedPlan));
        contributionRepository.saveAll(contributions);

        demand.setStatus(AggregationDemandStatus.PLAN_PROPOSED);

        return savedPlan;
    }

    private double distanceFromDemand(
            Crop crop,
            AggregationDemand demand
    ) {
        if (demand.getDeliveryLatitude() == null
                || demand.getDeliveryLongitude() == null
                || crop.getFarmer() == null
                || crop.getFarmer().getLatitude() == null
                || crop.getFarmer().getLongitude() == null) {
            return Double.MAX_VALUE;
        }

        return haversineKm(
                demand.getDeliveryLatitude(),
                demand.getDeliveryLongitude(),
                crop.getFarmer().getLatitude(),
                crop.getFarmer().getLongitude()
        );
    }

    private double haversineKm(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {
        double earthRadiusKm = 6371.0;

        double latDistance = Math.toRadians(latitude2 - latitude1);
        double lonDistance = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude1))
                * Math.cos(Math.toRadians(latitude2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        return earthRadiusKm * 2 * Math.atan2(
                Math.sqrt(a), Math.sqrt(1 - a));
    }
}