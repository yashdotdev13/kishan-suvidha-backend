package com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.repository;

import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.entity.Offer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.enums.OfferStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findByCropIdOrderByCreatedAtDesc(Long cropId);

    List<Offer> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Offer> findByCropFarmerIdOrderByCreatedAtDesc(Long farmerId);

    List<Offer> findByCropFarmerIdAndStatusOrderByCreatedAtDesc(
            Long farmerId,
            OfferStatus status
    );

    boolean existsByCropIdAndBuyerIdAndStatus(
            Long cropId,
            Long buyerId,
            OfferStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select o
        from Offer o
        where o.id = :id
        """)
    Optional<Offer> findByIdForUpdate(@Param("id") Long id);
}