package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository;


import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}