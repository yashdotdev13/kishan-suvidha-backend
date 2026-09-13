package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.dtos;


import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.enums.Role;

public record RegisterResponse(
        Long userId,
        String email,
        String phone,
        Role role,
        String message
) {
}
