package com.SmartIndiaHackathon.kishan_suvidha_backend.ai.tools;

import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.dtos.OfferResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MarketplaceTools {

    private final OfferService offerService;

    @Tool(description = "Get all marketplace offers received by the currently authenticated farmer for their crops")
    public List<OfferResponse> getMyReceivedOffers(ToolContext toolContext) {

        Object userIdValue = toolContext.getContext().get("userId");

        if (userIdValue == null) {
            throw new IllegalStateException("Authenticated user ID is missing");
        }

        Long userId = ((Number) userIdValue).longValue();
        return offerService.getMyReceivedOffers(userId);
    }
}