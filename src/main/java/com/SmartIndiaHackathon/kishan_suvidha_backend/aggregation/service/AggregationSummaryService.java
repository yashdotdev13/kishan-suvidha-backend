package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.dtos.AggregationSummaryResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationContribution;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationDemand;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationPlan;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationContributionStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationContributionRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationDemandRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AggregationSummaryService {

    private final AggregationDemandRepository demandRepository;
    private final AggregationPlanRepository planRepository;
    private final AggregationContributionRepository contributionRepository;

    @Transactional(readOnly = true)
    public AggregationSummaryResponse getSummary(Long demandId) {
        AggregationDemand demand = demandRepository.findById(demandId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Aggregation demand not found"));

        List<AggregationPlan> plans =
                planRepository.findByDemandIdOrderByCreatedAtDesc(demandId);

        BigDecimal plannedTotal = BigDecimal.ZERO;
        BigDecimal acceptedTotal = BigDecimal.ZERO;
        BigDecimal reservedTotal = BigDecimal.ZERO;

        for (AggregationPlan plan : plans) {
            List<AggregationContribution> contributions =
                    contributionRepository.findByPlanIdOrderByIdAsc(plan.getId());

            for (AggregationContribution contribution : contributions) {
                if (contribution.getStatus()
                        == AggregationContributionStatus.ACCEPTED
                        || contribution.getStatus()
                        == AggregationContributionStatus.RESERVED
                        || contribution.getStatus()
                        == AggregationContributionStatus.FULFILLED) {
                    acceptedTotal = acceptedTotal.add(
                            contribution.getProposedQuantityKg());
                }

                if (contribution.getStatus()
                        == AggregationContributionStatus.RESERVED
                        || contribution.getStatus()
                        == AggregationContributionStatus.FULFILLED) {
                    reservedTotal = reservedTotal.add(
                            contribution.getReservedQuantityKg());
                }
            }

            plannedTotal = plannedTotal.add(plan.getPlannedQuantityKg());
        }

        BigDecimal remaining = demand.getTargetQuantityKg()
                .subtract(reservedTotal)
                .max(BigDecimal.ZERO);

        List<AggregationSummaryResponse.PlanSummary> planSummaries =
                plans.stream()
                        .map(plan -> {
                            List<AggregationContribution> contributions =
                                    contributionRepository
                                            .findByPlanIdOrderByIdAsc(plan.getId());

                            BigDecimal accepted = contributions.stream()
                                    .filter(c ->
                                            c.getStatus()
                                                    == AggregationContributionStatus.ACCEPTED
                                                    || c.getStatus()
                                                    == AggregationContributionStatus.RESERVED
                                                    || c.getStatus()
                                                    == AggregationContributionStatus.FULFILLED)
                                    .map(AggregationContribution::getProposedQuantityKg)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            BigDecimal reserved = contributions.stream()
                                    .filter(c ->
                                            c.getStatus()
                                                    == AggregationContributionStatus.RESERVED
                                                    || c.getStatus()
                                                    == AggregationContributionStatus.FULFILLED)
                                    .map(AggregationContribution::getReservedQuantityKg)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            return new AggregationSummaryResponse.PlanSummary(
                                    plan.getId(),
                                    plan.getStatus(),
                                    plan.getPlannedQuantityKg(),
                                    accepted,
                                    reserved
                            );
                        })
                        .toList();

        return new AggregationSummaryResponse(
                demand.getId(),
                demand.getCropName(),
                demand.getTargetQuantityKg(),
                plannedTotal,
                acceptedTotal,
                reservedTotal,
                remaining,
                demand.getStatus(),
                planSummaries
        );
    }
}