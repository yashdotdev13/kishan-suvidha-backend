package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationDemand;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationDemandStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AggregationDemandRepository
        extends JpaRepository<AggregationDemand, Long> {

    Page<AggregationDemand> findByBuyerId(Long buyerId, Pageable pageable);

    List<AggregationDemand> findByStatusOrderByCreatedAtDesc(
            AggregationDemandStatus status
    );
}