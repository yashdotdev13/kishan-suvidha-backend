package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity.Crop;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.repository.CropRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationContribution;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationPlan;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationContributionStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationPlanStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationContributionRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AggregationCancellationService {

    private final AggregationPlanRepository planRepository;
    private final AggregationContributionRepository contributionRepository;
    private final CropRepository cropRepository;
    private final CropQuantityConverter quantityConverter;

    @Transactional
    public AggregationPlan cancelPlan(Long planId) {
        AggregationPlan plan = planRepository.findById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Aggregation plan not found"));

        if (plan.getStatus() == AggregationPlanStatus.CANCELLED
                || plan.getStatus() == AggregationPlanStatus.FULFILLED) {
            throw new IllegalStateException(
                    "This plan cannot be cancelled");
        }

        if (plan.getStatus() == AggregationPlanStatus.RESERVED
                || plan.getStatus() == AggregationPlanStatus.PARTIALLY_FULFILLED) {
            releaseReservations(planId);
        }

        List<AggregationContribution> contributions =
                contributionRepository.findByPlanIdOrderByIdAsc(planId);

        for (AggregationContribution contribution : contributions) {
            if (contribution.getStatus()
                    != AggregationContributionStatus.FULFILLED) {
                contribution.setStatus(
                        AggregationContributionStatus.CANCELLED);
            }
        }

        plan.setStatus(AggregationPlanStatus.CANCELLED);

        return planRepository.save(plan);
    }

    @Transactional
    public void releaseReservations(Long planId) {
        AggregationPlan plan = planRepository.findById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Aggregation plan not found"));

        List<AggregationContribution> contributions =
                contributionRepository.findByPlanIdOrderByIdAsc(planId)
                        .stream()
                        .filter(c -> c.getStatus()
                                == AggregationContributionStatus.RESERVED)
                        .sorted(Comparator.comparing(
                                c -> c.getCrop().getId()))
                        .toList();

        for (AggregationContribution contribution : contributions) {
            Crop crop = cropRepository.findByIdForUpdate(
                            contribution.getCrop().getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Crop listing not found"));

            BigDecimal reservedKg = contribution.getReservedQuantityKg();

            if (reservedKg == null || reservedKg.signum() <= 0) {
                continue;
            }

            BigDecimal currentKg = quantityConverter.toKilograms(
                    crop.getQuantity(), crop.getUnit());

            BigDecimal restoredKg = currentKg.add(reservedKg);

            BigDecimal restoredOriginalUnit = switch (crop.getUnit()) {
                case KG -> restoredKg;
                case QUINTAL -> restoredKg.divide(
                        BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP);
                case TON -> restoredKg.divide(
                        BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            };

            crop.setQuantity(restoredOriginalUnit);

            if (crop.getStatus() == CropStatus.RESERVED) {
                crop.setStatus(CropStatus.AVAILABLE);
            }

            contribution.setReservedQuantityKg(BigDecimal.ZERO);
            contribution.setStatus(AggregationContributionStatus.RELEASED);
        }

        contributionRepository.saveAll(contributions);

        if (plan.getStatus() != AggregationPlanStatus.CANCELLED) {
            plan.setStatus(AggregationPlanStatus.CANCELLED);
            planRepository.save(plan);
        }
    }
}