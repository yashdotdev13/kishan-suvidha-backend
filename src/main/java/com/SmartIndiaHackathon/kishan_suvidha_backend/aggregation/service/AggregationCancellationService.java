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

    private static final BigDecimal KG_PER_QUINTAL = BigDecimal.valueOf(100);
    private static final BigDecimal KG_PER_TON = BigDecimal.valueOf(1000);

    private final AggregationPlanRepository planRepository;
    private final AggregationContributionRepository contributionRepository;
    private final CropRepository cropRepository;
    private final CropQuantityConverter quantityConverter;

    @Transactional
    public AggregationPlan cancelPlan(Long planId) {
        AggregationPlan plan = planRepository.findById(planId).orElseThrow(()
                -> new IllegalArgumentException("Aggregation plan not found"));
        if (plan.getStatus() == AggregationPlanStatus.CANCELLED || plan.getStatus()
                == AggregationPlanStatus.FULFILLED) {
            throw new IllegalStateException("This plan cannot be cancelled");
        }

        List<AggregationContribution> contributions = contributionRepository.findByPlanIdOrderByIdAsc(planId);
        boolean hasFulfilledContribution = contributions.stream().anyMatch(c -> c.getStatus()
                == AggregationContributionStatus.FULFILLED);
        if (hasFulfilledContribution) {
            throw new IllegalStateException("A plan containing fulfilled contributions cannot be cancelled");
        }
        // Restore reserved stock before marking the plan cancelled.
        restoreReservedStock(plan, contributions);

        for (AggregationContribution contribution : contributions) {
            if (contribution.getStatus() != AggregationContributionStatus.FULFILLED) {
                contribution.setStatus(AggregationContributionStatus.CANCELLED);
            }
        }
        plan.setStatus(AggregationPlanStatus.CANCELLED);
        contributionRepository.saveAll(contributions);
        return planRepository.save(plan);
    }

    private void restoreReservedStock(AggregationPlan plan, List<AggregationContribution> contributions) {

        if (plan.getStatus() != AggregationPlanStatus.RESERVED && plan.getStatus() != AggregationPlanStatus.PARTIALLY_FULFILLED) {
            return;
        }
        List<AggregationContribution> reservedContributions = contributions.stream()
                .filter(c ->
                        c.getStatus() == AggregationContributionStatus.RESERVED)
                .sorted(Comparator.comparing(c -> c.getCrop().getId())).toList();

        for (AggregationContribution contribution : reservedContributions) {
            BigDecimal reservedKg = contribution.getReservedQuantityKg();

            if (reservedKg == null || reservedKg.signum() <= 0) {
                throw new IllegalStateException("Invalid reserved quantity for contribution " +
                        contribution.getId());
            }
            Crop crop = cropRepository.findByIdForUpdate(contribution.getCrop().getId()).orElseThrow(()
                    -> new IllegalArgumentException("Crop listing not found"));

            // Never reactivate a sold or cancelled crop listing.
            if (crop.getStatus() == CropStatus.SOLD || crop.getStatus() == CropStatus.CANCELLED) {
                throw new IllegalStateException("Cannot restore stock to a sold or cancelled crop listing: "
                        + crop.getId());
            }
            BigDecimal currentKg = quantityConverter.toKilograms(crop.getQuantity(), crop.getUnit());
            BigDecimal restoredKg = currentKg.add(reservedKg);
            BigDecimal restoredOriginalUnit = switch (crop.getUnit()) {
                case KG -> restoredKg.setScale(3, RoundingMode.UNNECESSARY);
                case QUINTAL -> restoredKg.divide(KG_PER_QUINTAL, 3, RoundingMode.UNNECESSARY);
                case TON -> restoredKg.divide(KG_PER_TON, 3, RoundingMode.UNNECESSARY);
            };
            BigDecimal verifiedRestoredKg = quantityConverter.toKilograms(restoredOriginalUnit, crop.getUnit());

            if (verifiedRestoredKg.compareTo(restoredKg) != 0) {
                throw new IllegalStateException("Restoring this reservation would lose quantity precision "
                        + "for crop " + crop.getId());
            }
            crop.setQuantity(restoredOriginalUnit);
            if (crop.getStatus() == CropStatus.RESERVED) {
                crop.setStatus(CropStatus.AVAILABLE);
            }
            // Clearing the reservation makes it impossible for a subsequent
            // cancellation to restore the same contribution a second time.
            contribution.setReservedQuantityKg(BigDecimal.ZERO);
            contribution.setStatus(AggregationContributionStatus.RELEASED);
        }
    }
}
