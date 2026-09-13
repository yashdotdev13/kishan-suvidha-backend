package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository;


import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Officer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfficerRepository extends JpaRepository<Officer, Long> {

    Optional<Officer> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}