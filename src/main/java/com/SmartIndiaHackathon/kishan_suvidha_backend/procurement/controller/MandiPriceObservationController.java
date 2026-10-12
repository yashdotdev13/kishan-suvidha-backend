package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.controller;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.CreateMandiPriceObservationRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.MandiPriceObservationResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.MandiPriceObservation;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.MandiPriceObservationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/mandi-prices")
public class MandiPriceObservationController {

    private final MandiPriceObservationService service;

    public MandiPriceObservationController(
            MandiPriceObservationService service
    ) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OFFICER')")
    public MandiPriceObservationResponse create(
            @Valid @RequestBody CreateMandiPriceObservationRequest request
    ) {
        MandiPriceObservation observation = service.create(request);
        return MandiPriceObservationResponse.from(observation);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FARMER', 'BUYER', 'OFFICER')")
    public List<MandiPriceObservationResponse> getHistoricalPrices(
            @RequestParam String cropName,
            @RequestParam String state,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        return service.getHistoricalPrices(cropName, state, from, to)
                .stream()
                .map(MandiPriceObservationResponse::from)
                .toList();
    }

    @GetMapping("/market")
    @PreAuthorize("hasAnyRole('FARMER', 'BUYER', 'OFFICER')")
    public List<MandiPriceObservationResponse> getMarketHistoricalPrices(
            @RequestParam String cropName,
            @RequestParam String marketName,
            @RequestParam String state,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        return service.getMarketHistoricalPrices(
                        cropName, marketName, state, from, to
                )
                .stream()
                .map(MandiPriceObservationResponse::from)
                .toList();
    }
}