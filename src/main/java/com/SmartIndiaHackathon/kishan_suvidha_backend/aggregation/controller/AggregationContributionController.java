package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.controller;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationContribution;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.service.AggregationContributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aggregation/contributions")
@RequiredArgsConstructor
public class AggregationContributionController {

    private final AggregationContributionService contributionService;

    @GetMapping("/my/pending")
    @PreAuthorize("hasRole('FARMER')")
    public List<AggregationContribution> getMyPendingContributions() {
        return contributionService.getMyPendingContributions();
    }

    @PostMapping("/{contributionId}/accept")
    @PreAuthorize("hasRole('FARMER')")
    public AggregationContribution accept(
            @PathVariable Long contributionId
    ) {
        return contributionService.respond(contributionId, true);
    }

    @PostMapping("/{contributionId}/reject")
    @PreAuthorize("hasRole('FARMER')")
    public AggregationContribution reject(
            @PathVariable Long contributionId
    ) {
        return contributionService.respond(contributionId, false);
    }
}