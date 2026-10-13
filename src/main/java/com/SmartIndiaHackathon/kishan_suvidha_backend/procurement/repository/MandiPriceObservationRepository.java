
package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.MandiPriceObservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MandiPriceObservationRepository
        extends JpaRepository<MandiPriceObservation, Long> {

    boolean existsBySourceAndSourceRecordId(
            String source,
            String sourceRecordId
    );

    List<MandiPriceObservation>
    findByCropNameIgnoreCaseAndStateIgnoreCaseAndObservationDateBetweenOrderByObservationDateAsc(
            String cropName,
            String state,
            LocalDate from,
            LocalDate to
    );

    List<MandiPriceObservation>
    findByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseAndObservationDateBetweenOrderByObservationDateAsc(
            String cropName,
            String marketName,
            String state,
            LocalDate from,
            LocalDate to
    );

    Optional<MandiPriceObservation>
    findFirstByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseOrderByObservationDateDescIdDesc(
            String cropName,
            String marketName,
            String state
    );
}
