package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.dtos;


import jakarta.validation.constraints.NotBlank;

public record UpdateLocationRequest(

        @NotBlank(message = "Location is required")
        String location
) {
}
