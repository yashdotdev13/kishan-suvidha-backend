package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.controller;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.dtos.CreateAggregationDemandRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationDemand;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.service.AggregationDemandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/aggregation/demands")
@RequiredArgsConstructor
public class AggregationDemandController {

    private final AggregationDemandService demandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BUYER')")
    public AggregationDemand createDemand(
            @Valid @RequestBody CreateAggregationDemandRequest request
    ) {
        Long buyerId = demandService.getAuthenticatedBuyerId();
        return demandService.createDemand(buyerId, request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('BUYER')")
    public Page<AggregationDemand> getMyDemands(Pageable pageable) {
        Long buyerId = demandService.getAuthenticatedBuyerId();
        return demandService.getBuyerDemands(buyerId, pageable);
    }

    @GetMapping("/{demandId}")
    public AggregationDemand getDemand(@PathVariable Long demandId) {
        return demandService.getDemand(demandId);
    }

}