package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.ProcurementCentre;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcurementCentreRepository
        extends JpaRepository<ProcurementCentre, Long> {

    List<ProcurementCentre> findByState(String state);

    List<ProcurementCentre> findByDistrict(String district);

    List<ProcurementCentre> findByStateAndDistrict(
            String state,
            String district
    );

    List<ProcurementCentre> findByAcceptingQueueTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from ProcurementCentre c
            where c.id = :id
            """)
    Optional<ProcurementCentre> findByIdForUpdate(
            @Param("id") Long id
    );
}