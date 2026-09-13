package com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.repository;


import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity.Crop;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {

    List<Crop> findByFarmerId(Long farmerId);


    Optional<Crop> findByIdAndFarmerId(Long cropId, Long farmerId);

    List<Crop> findByFarmerIdAndStatus(
            Long farmerId,
            CropStatus status
    );

    List<Crop> findByStatus(CropStatus status);

    boolean existsByIdAndFarmerId(
            Long cropId,
            Long farmerId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select c
        from Crop c
        where c.id = :id
        """)
    Optional<Crop> findByIdForUpdate(@Param("id") Long id);
}