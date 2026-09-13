package com.SmartIndiaHackathon.kishan_suvidha_backend.ai.controller;


import com.SmartIndiaHackathon.kishan_suvidha_backend.ai.dtos.AiChatRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.ai.dtos.AiChatResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.ai.service.KisanAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final KisanAiService kisanAiService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @Valid @RequestBody AiChatRequest request
    ) {

        String response = kisanAiService.chat(request.message());

        return ResponseEntity.ok(
                new AiChatResponse(response)
        );
    }
}
