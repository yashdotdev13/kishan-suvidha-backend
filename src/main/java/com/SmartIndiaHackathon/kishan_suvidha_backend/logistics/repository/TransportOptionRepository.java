package com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.repository;

import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.entity.TransportOption;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.enums.VehicleType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransportOptionRepository
        extends JpaRepository<TransportOption, Long> {

    List<TransportOption> findByAvailableTrue();

    List<TransportOption> findByVehicleTypeAndAvailableTrue(
            VehicleType vehicleType
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select t
            from TransportOption t
            where t.id = :id
            """)
    Optional<TransportOption> findByIdForUpdate(
            @Param("id") Long id
    );
}