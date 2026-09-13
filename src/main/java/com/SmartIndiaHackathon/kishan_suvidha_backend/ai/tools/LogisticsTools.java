package com.SmartIndiaHackathon.kishan_suvidha_backend.ai.tools;

import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos.TransportCostEstimateResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos.TransportOptionResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.service.TransportCostService;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.service.TransportOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LogisticsTools {

    private final TransportOptionService transportOptionService;
    private final TransportCostService transportCostService;

    @Tool(description = "Get all currently available transport options for crop transportation")
    public List<TransportOptionResponse> getAvailableTransportOptions() {
        return transportOptionService.getAvailable();
    }

    @Tool(description = "Estimate the transport cost for the authenticated farmer to a procurement centre using a selected available transport option and crop quantity in quintals")
    public TransportCostEstimateResponse estimateTransportCostToProcurementCentre(
            Long procurementCentreId,
            Long transportOptionId,
            BigDecimal quantityQuintals,
            ToolContext toolContext
    ) {

        Object userIdValue =
                toolContext.getContext().get("userId");

        if (userIdValue == null) {
            throw new IllegalStateException(
                    "Authenticated user ID is missing"
            );
        }

        Long userId =
                ((Number) userIdValue).longValue();

        return transportCostService
                .estimateToProcurementCentre(
                        userId,
                        procurementCentreId,
                        transportOptionId,
                        quantityQuintals
                );
    }

    @Tool(description = """
        Estimate the transport cost for an offer received by the
        authenticated farmer.

        Use this when comparing a marketplace offer with another
        selling option. The transport cost is calculated from the
        farmer's location to the buyer's location using the selected
        available transport option.

        Only use this for offers belonging to the authenticated farmer.
        """)
    public TransportCostEstimateResponse estimateMarketplaceTransportCost(
            Long offerId,
            Long transportOptionId,
            ToolContext toolContext
    ) {
        Object userIdValue = toolContext.getContext().get("userId");

        if (userIdValue == null) {
            throw new IllegalStateException(
                    "Authenticated user ID is missing"
            );
        }

        Long userId = ((Number) userIdValue).longValue();

        return transportCostService.estimateForMarketplaceOffer(
                userId,
                offerId,
                transportOptionId
        );
    }
}