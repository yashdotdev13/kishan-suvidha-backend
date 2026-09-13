package com.SmartIndiaHackathon.kishan_suvidha_backend.ai.tools;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.dtos.CropResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.service.CropService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CropTools {

    private final CropService cropService;

    @Tool(description = "Get all crops registered by the currently authenticated farmer")
    public List<CropResponse> getMyCrops(ToolContext toolContext) {

        Object userIdValue = toolContext.getContext().get("userId");
        if (userIdValue == null) {
            throw new IllegalStateException("Authenticated user ID is missing");
        }
        Long userId = ((Number) userIdValue).longValue();
        return cropService.getMyCrops(userId);
    }
}