package com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.repository;

import com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.entity.Transaction;
import com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.enums.TransactionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByOfferId(Long offerId);

    List<Transaction> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);

    List<Transaction> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Transaction> findByStatusOrderByCreatedAtDesc(
            TransactionStatus status
    );

    boolean existsByOfferId(Long offerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.id = :transactionId
            """)
    Optional<Transaction> findByIdForUpdate(
            @Param("transactionId") Long transactionId
    );
}