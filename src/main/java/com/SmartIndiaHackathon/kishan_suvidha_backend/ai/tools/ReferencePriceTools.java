package com.SmartIndiaHackathon.kishan_suvidha_backend.ai.tools;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.ReferencePriceResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.ReferencePriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReferencePriceTools {

    private final ReferencePriceService referencePriceService;

    @Tool(description = """
            Get the currently active reference price per quintal
            for a specific crop and state.
            Use this when the user asks about the benchmark/reference
            price for a crop or when comparing a marketplace offer
            against the procurement reference price.
            """)
    public ReferencePriceResponse getCurrentReferencePrice(
            String cropName,
            String state
    ) {
        return referencePriceService.getCurrentPrice(
                cropName,
                state
        );
    }
}