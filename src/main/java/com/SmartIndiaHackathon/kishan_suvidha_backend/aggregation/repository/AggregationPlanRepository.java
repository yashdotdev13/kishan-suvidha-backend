package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AggregationPlanRepository
        extends JpaRepository<AggregationPlan, Long> {

    List<AggregationPlan> findByDemandIdOrderByCreatedAtDesc(Long demandId);
}