package com.SmartIndiaHackathon.kishan_suvidha_backend.ai.tools;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.ProcurementCentreResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.ProcurementCentreService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProcurementTools {

    private final ProcurementCentreService procurementCentreService;

    @Tool(description = "Get all procurement centres available in Kisan Suvidha")
    public List<ProcurementCentreResponse> getAllProcurementCentres() {
        return procurementCentreService.getAllCentres();
    }
}