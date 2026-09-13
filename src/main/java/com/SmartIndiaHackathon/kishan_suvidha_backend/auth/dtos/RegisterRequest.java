package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.dtos;


import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Phone is required")
        @Size(min = 10, max = 15, message = "Phone must contain 10 to 15 digits")
        String phone,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must contain at least 8 characters")
        String password,

        @NotNull(message = "Role is required")
        Role role,

        @NotBlank(message = "Name is required")
        String name,

        String companyName,

        String state,

        String district,

        String village,

        String location
) {
}