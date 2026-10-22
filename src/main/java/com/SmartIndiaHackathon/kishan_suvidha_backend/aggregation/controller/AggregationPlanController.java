package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.controller;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.dtos.AggregationSummaryResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationContribution;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationPlan;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationContributionRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationPlanRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aggregation")
@RequiredArgsConstructor
public class AggregationPlanController {

    private final AggregationMatchingService matchingService;
    private final AggregationDemandService demandService;
    private final AggregationPlanRepository planRepository;
    private final AggregationContributionRepository contributionRepository;
    private final AggregationReservationService reservationService;
    private final AggregationSummaryService aggregationSummaryService;
    private final AggregationCancellationService cancellationService;

    @PostMapping("/demands/{demandId}/plans")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BUYER')")
    public AggregationPlan generatePlan(@PathVariable Long demandId) {
        Long buyerId = demandService.getAuthenticatedBuyerId();
        var demand = demandService.getDemand(demandId);
        if (!demand.getBuyer().getId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot generate a plan for another buyer's demand");
        }
        return matchingService.generatePlan(demandId);
    }

    @GetMapping("/demands/{demandId}/plans")
    @PreAuthorize("hasRole('BUYER')")
    public List<AggregationPlan> getPlans(@PathVariable Long demandId) {
        Long buyerId = demandService.getAuthenticatedBuyerId();
        var demand = demandService.getDemand(demandId);
        if (!demand.getBuyer().getId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot view another buyer's demand plans");
        }
        return planRepository.findByDemandIdOrderByCreatedAtDesc(demandId);
    }

    @GetMapping("/plans/{planId}/contributions")
    @PreAuthorize("hasRole('BUYER')")
    public List<AggregationContribution> getContributions(@PathVariable Long planId) {
        AggregationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aggregation plan not found"));
        Long buyerId = demandService.getAuthenticatedBuyerId();
        if (!plan.getDemand().getBuyer().getId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot view another buyer's plan contributions");
        }
        return contributionRepository.findByPlanIdOrderByIdAsc(planId);
    }

    @PostMapping("/plans/{planId}/reserve")
    @PreAuthorize("hasRole('BUYER')")
    public AggregationPlan reservePlan(@PathVariable Long planId) {
        Long buyerId = demandService.getAuthenticatedBuyerId();
        AggregationPlan plan = planRepository.findById(planId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aggregation plan not found"));
        if (!plan.getDemand().getBuyer().getId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot reserve another buyer's plan");
        }
        return reservationService.reserveAcceptedContributions(planId);
    }

    @GetMapping("/demands/{demandId}/summary")
    @PreAuthorize("hasRole('BUYER')")
    public AggregationSummaryResponse getSummary(@PathVariable Long demandId) {
        Long buyerId = demandService.getAuthenticatedBuyerId();
        var demand = demandService.getDemand(demandId);
        if (!demand.getBuyer().getId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot view another buyer's aggregation summary");
        }
        return aggregationSummaryService.getSummary(demandId);
    }


    @PostMapping("/plans/{planId}/cancel")
    @PreAuthorize("hasRole('BUYER')")
    public AggregationPlan cancelPlan(@PathVariable Long planId) {
        Long buyerId = demandService.getAuthenticatedBuyerId();
        AggregationPlan plan = planRepository.findById(planId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aggregation plan not found"));
        if (!plan.getDemand().getBuyer().getId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot cancel another buyer's plan");
        }
        return cancellationService.cancelPlan(planId);
    }

}