package com.SmartIndiaHackathon.kishan_suvidha_backend.ai.tools;

import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos.TransportOptionResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.service.TransportOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LogisticsTools {

    private final TransportOptionService transportOptionService;

    @Tool(description = "Get all currently available transport options for crop transportation, including vehicle type, vehicle number, capacity, and transportation rate")
    public List<TransportOptionResponse> getAvailableTransportOptions() {
        return transportOptionService.getAvailable();
    }
}