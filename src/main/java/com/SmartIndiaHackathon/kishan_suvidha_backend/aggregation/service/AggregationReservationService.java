
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

    private static final BigDecimal KG_PER_QUINTAL = BigDecimal.valueOf(100);
    private static final BigDecimal KG_PER_TON = BigDecimal.valueOf(1000);

    private final AggregationPlanRepository planRepository;
    private final AggregationContributionRepository contributionRepository;
    private final CropRepository cropRepository;
    private final CropQuantityConverter quantityConverter;

    @Transactional
    public AggregationPlan reserveAcceptedContributions(Long planId) {
        AggregationPlan plan = planRepository.findById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Aggregation plan not found"));

        // Reservation is allowed only after all farmer responses are received.
        if (plan.getStatus() != AggregationPlanStatus.READY_TO_RESERVE) {
            throw new IllegalStateException(
                    "Plan can only be reserved after all farmer responses are received");
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
            BigDecimal requestedKg = contribution.getProposedQuantityKg();

            if (requestedKg == null || requestedKg.signum() <= 0) {
                throw new IllegalStateException(
                        "Contribution quantity must be greater than zero");
            }

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

            // Never reserve more than the stock currently available.
            BigDecimal actualReservedKg = requestedKg.min(availableKg);

            if (actualReservedKg.signum() <= 0) {
                contribution.setStatus(AggregationContributionStatus.RELEASED);
                contribution.setReservedQuantityKg(BigDecimal.ZERO);
                continue;
            }

            BigDecimal remainingKg = availableKg.subtract(actualReservedKg);

            // Convert back without rounding remaining stock upward.
            BigDecimal remainingInOriginalUnit = switch (crop.getUnit()) {
                case KG -> remainingKg.setScale(3, RoundingMode.DOWN);
                case QUINTAL -> remainingKg.divide(
                        KG_PER_QUINTAL, 3, RoundingMode.DOWN);
                case TON -> remainingKg.divide(
                        KG_PER_TON, 3, RoundingMode.DOWN);
            };

            // Do not silently discard stock that cannot be represented
            // precisely in the crop's original unit.
            BigDecimal representedRemainingKg = quantityConverter.toKilograms(
                    remainingInOriginalUnit, crop.getUnit());

            if (representedRemainingKg.compareTo(remainingKg) != 0) {
                throw new IllegalStateException(
                        "Reservation would lose stock precision for crop ID "
                                + crop.getId()
                                + ". Store crop quantities in kilograms or use "
                                + "a more precise quantity representation.");
            }

            crop.setQuantity(remainingInOriginalUnit);

            if (remainingInOriginalUnit.signum() == 0) {
                crop.setStatus(CropStatus.RESERVED);
            }

            contribution.setReservedQuantityKg(actualReservedKg);
            contribution.setStatus(AggregationContributionStatus.RESERVED);

            reservedTotalKg = reservedTotalKg.add(actualReservedKg);
        }

        if (reservedTotalKg.signum() == 0) {
            throw new IllegalStateException(
                    "No crop stock could be reserved; no reservation was made");
        }

        contributionRepository.saveAll(contributions);

        plan.setPlannedQuantityKg(reservedTotalKg);
        plan.setStatus(
                reservedTotalKg.compareTo(
                        plan.getDemand().getTargetQuantityKg()) >= 0
                        ? AggregationPlanStatus.RESERVED
                        : AggregationPlanStatus.PARTIALLY_FULFILLED
        );

        return planRepository.save(plan);
    }
}
