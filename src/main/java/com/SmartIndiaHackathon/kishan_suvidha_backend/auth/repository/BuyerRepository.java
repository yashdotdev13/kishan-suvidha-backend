package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository;


import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuyerRepository extends JpaRepository<Buyer, Long> {

    Optional<Buyer> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}