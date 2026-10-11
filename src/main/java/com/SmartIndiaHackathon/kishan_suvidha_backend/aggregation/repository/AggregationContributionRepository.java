package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationContribution;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationContributionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AggregationContributionRepository
        extends JpaRepository<AggregationContribution, Long> {

    List<AggregationContribution> findByPlanIdOrderByIdAsc(Long planId);

    List<AggregationContribution> findByCropIdAndStatus(
            Long cropId,
            AggregationContributionStatus status
    );

    boolean existsByPlanIdAndCropId(Long planId, Long cropId);

    List<AggregationContribution> findByCropFarmerUserIdAndStatusOrderByCreatedAtDesc(Long id, AggregationContributionStatus aggregationContributionStatus);
}