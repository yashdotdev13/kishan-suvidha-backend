package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository;


import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {

    Optional<Farmer> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}