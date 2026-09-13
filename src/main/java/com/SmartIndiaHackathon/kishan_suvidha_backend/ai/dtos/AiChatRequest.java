package com.SmartIndiaHackathon.kishan_suvidha_backend.ai.dtos;


import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(

        @NotBlank(message = "Message is required")
        String message

) {
}