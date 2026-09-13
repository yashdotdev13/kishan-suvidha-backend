package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository;


import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.ReferencePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReferencePriceRepository
        extends JpaRepository<ReferencePrice, Long> {

    List<ReferencePrice> findByCropNameIgnoreCaseAndStateIgnoreCase(
            String cropName,
            String state
    );

    @Query("""
            SELECT r
            FROM ReferencePrice r
            WHERE LOWER(r.cropName) = LOWER(:cropName)
              AND LOWER(r.state) = LOWER(:state)
              AND r.effectiveFrom <= :date
              AND (r.effectiveTo IS NULL OR r.effectiveTo >= :date)
            ORDER BY r.effectiveFrom DESC
            """)
    List<ReferencePrice> findCurrentPrices(
            @Param("cropName") String cropName,
            @Param("state") String state,
            @Param("date") LocalDate date
    );
}