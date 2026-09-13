package com.SmartIndiaHackathon.kishan_suvidha_backend.ai.tools;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.QueueEntryResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QueueTools {

    private final QueueService queueService;

    @Tool(description = "Get the current procurement queue status of the authenticated farmer, including token number, farmers ahead, estimated waiting time, crop, procurement centre, and queue status")
    public List<QueueEntryResponse> getMyQueueStatus(ToolContext toolContext) {

        Object userIdValue = toolContext.getContext().get("userId");
        if (userIdValue == null) {
            throw new IllegalStateException("Authenticated user ID is missing");
        }

        Long userId = ((Number) userIdValue).longValue();
        return queueService.getMyQueues(userId);
    }
}