package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.QueueEntry;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QueueEntryRepository
        extends JpaRepository<QueueEntry, Long> {

    List<QueueEntry> findByProcurementCentreIdAndStatusOrderByTokenNumberAsc(
            Long procurementCentreId,
            QueueStatus status
    );

    List<QueueEntry> findByFarmerIdAndStatus(
            Long farmerId,
            QueueStatus status
    );

    List<QueueEntry> findByFarmerIdAndStatusIn(
            Long farmerId,
            Collection<QueueStatus> statuses
    );

    Optional<QueueEntry> findByFarmerIdAndCropIdAndStatus(
            Long farmerId,
            Long cropId,
            QueueStatus status
    );

    boolean existsByFarmerIdAndCropIdAndStatus(
            Long farmerId,
            Long cropId,
            QueueStatus status
    );

    Optional<QueueEntry> findFirstByProcurementCentreIdAndStatus(
            Long procurementCentreId,
            QueueStatus status
    );

    Optional<QueueEntry> findFirstByProcurementCentreIdAndStatusOrderByTokenNumberAsc(
            Long procurementCentreId,
            QueueStatus status
    );
}