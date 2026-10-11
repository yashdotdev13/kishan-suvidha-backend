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
public class AggregationReservationService {

    private final AggregationPlanRepository planRepository;
    private final AggregationContributionRepository contributionRepository;
    private final CropRepository cropRepository;
    private final CropQuantityConverter quantityConverter;

    @Transactional
    public AggregationPlan reserveAcceptedContributions(Long planId) {
        AggregationPlan plan = planRepository.findById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Aggregation plan not found"));

        if (plan.getStatus() != AggregationPlanStatus.AWAITING_FARMER_APPROVALS
                && plan.getStatus() != AggregationPlanStatus.READY_TO_RESERVE) {
            throw new IllegalStateException(
                    "Plan is not ready for reservation");
        }

        List<AggregationContribution> contributions =
                contributionRepository.findByPlanIdOrderByIdAsc(planId);

        List<AggregationContribution> accepted = contributions.stream()
                .filter(c -> c.getStatus() == AggregationContributionStatus.ACCEPTED)
                .sorted(Comparator.comparing(c -> c.getCrop().getId()))
                .toList();

        if (accepted.isEmpty()) {
            throw new IllegalStateException(
                    "No farmer contributions have been accepted");
        }

        BigDecimal reservedTotalKg = BigDecimal.ZERO;

        for (AggregationContribution contribution : accepted) {
            Crop crop = cropRepository.findByIdForUpdate(
                            contribution.getCrop().getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Crop listing not found"));

            if (crop.getStatus() != CropStatus.AVAILABLE) {
                contribution.setStatus(AggregationContributionStatus.RELEASED);
                contribution.setReservedQuantityKg(BigDecimal.ZERO);
                continue;
            }

            BigDecimal availableKg = quantityConverter.toKilograms(
                    crop.getQuantity(), crop.getUnit());

            BigDecimal requestedKg = contribution.getProposedQuantityKg();

            if (requestedKg == null || requestedKg.signum() <= 0) {
                throw new IllegalStateException(
                        "Contribution quantity must be greater than zero");
            }

            // Never reserve more than the stock currently available.
            BigDecimal actualReservedKg = requestedKg.min(availableKg);

            if (actualReservedKg.signum() <= 0) {
                contribution.setStatus(AggregationContributionStatus.RELEASED);
                contribution.setReservedQuantityKg(BigDecimal.ZERO);
                continue;
            }

            BigDecimal remainingKg = availableKg.subtract(actualReservedKg);
            BigDecimal remainingInOriginalUnit = switch (crop.getUnit()) {
                case KG -> remainingKg;
                case QUINTAL -> remainingKg.divide(
                        BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP);
                case TON -> remainingKg.divide(
                        BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            };

            crop.setQuantity(remainingInOriginalUnit);

            if (remainingKg.signum() == 0) {
                crop.setStatus(CropStatus.RESERVED);
            }

            contribution.setReservedQuantityKg(actualReservedKg);
            contribution.setStatus(AggregationContributionStatus.RESERVED);

            reservedTotalKg = reservedTotalKg.add(actualReservedKg);
        }

        contributionRepository.saveAll(contributions);

        if (reservedTotalKg.signum() == 0) {
            throw new IllegalStateException(
                    "No crop stock could be reserved; the plan remains unchanged");
        }

        plan.setPlannedQuantityKg(reservedTotalKg);
        plan.setStatus(
                reservedTotalKg.compareTo(plan.getDemand().getTargetQuantityKg()) >= 0
                        ? AggregationPlanStatus.RESERVED
                        : AggregationPlanStatus.PARTIALLY_FULFILLED
        );

        return planRepository.save(plan);
    }
}