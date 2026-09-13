package com.SmartIndiaHackathon.kishan_suvidha_backend.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KisanAiService {

    private final ChatClient chatClient;

    public String chat(String message) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long userId = (Long) authentication.getPrincipal();

        return chatClient
                .prompt()
                .user(message)
                .toolContext(Map.of("userId", userId))
                .call()
                .content();
    }
}