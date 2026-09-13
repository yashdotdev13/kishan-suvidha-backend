package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.dtos;


import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.enums.Role;

public record LoginResponse(

        String accessToken,

        String tokenType,

        Long userId,

        String email,

        Role role
) {
}