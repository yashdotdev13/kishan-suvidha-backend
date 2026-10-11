package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationContribution;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationPlan;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationContributionStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationPlanStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationContributionRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.User;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AggregationContributionService {

    private final AggregationContributionRepository contributionRepository;
    private final UserRepository userRepository;

    @Transactional
    public AggregationContribution respond(
            Long contributionId,
            boolean accept
    ) {
        User user = getAuthenticatedUser();

        if (user.getRole() == null
                || !user.getRole().name().equals("FARMER")) {
            throw new AccessDeniedException(
                    "Only farmers can respond to contributions");
        }

        AggregationContribution contribution = contributionRepository
                .findById(contributionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Contribution not found"));

        if (contribution.getCrop().getFarmer() == null
                || contribution.getCrop().getFarmer().getUser() == null
                || !contribution.getCrop().getFarmer().getUser().getId()
                .equals(user.getId())) {
            throw new AccessDeniedException(
                    "You cannot respond to another farmer's contribution");
        }

        if (contribution.getStatus()
                != AggregationContributionStatus.PROPOSED) {
            throw new IllegalStateException(
                    "This contribution has already been responded to");
        }

        AggregationPlan plan = contribution.getPlan();

        if (plan.getStatus() != AggregationPlanStatus.PROPOSED
                && plan.getStatus()
                != AggregationPlanStatus.AWAITING_FARMER_APPROVALS) {
            throw new IllegalStateException(
                    "This aggregation plan is not accepting farmer responses");
        }

        contribution.setStatus(
                accept
                        ? AggregationContributionStatus.ACCEPTED
                        : AggregationContributionStatus.REJECTED
        );
        contribution.setRespondedAt(LocalDateTime.now());

        AggregationContribution savedContribution =
                contributionRepository.save(contribution);

        List<AggregationContribution> contributions =
                contributionRepository.findByPlanIdOrderByIdAsc(plan.getId());

        boolean allResponded = contributions.stream()
                .allMatch(item ->
                        item.getStatus()
                                != AggregationContributionStatus.PROPOSED);

        boolean anyAccepted = contributions.stream()
                .anyMatch(item ->
                        item.getStatus()
                                == AggregationContributionStatus.ACCEPTED);

        if (allResponded) {
            plan.setStatus(
                    anyAccepted
                            ? AggregationPlanStatus.READY_TO_RESERVE
                            : AggregationPlanStatus.REJECTED
            );
        }
        else {
            plan.setStatus(
                    AggregationPlanStatus.AWAITING_FARMER_APPROVALS
            );
        }

        return savedContribution;
    }

    @Transactional(readOnly = true)
    public List<AggregationContribution> getMyPendingContributions() {
        User user = getAuthenticatedUser();

        if (user.getRole() == null
                || !user.getRole().name().equals("FARMER")) {
            throw new AccessDeniedException(
                    "Only farmers can view their pending contributions");
        }

        return contributionRepository
                .findByCropFarmerUserIdAndStatusOrderByCreatedAtDesc(
                        user.getId(),
                        AggregationContributionStatus.PROPOSED
                );
    }

    private User getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new AccessDeniedException(
                    "Authentication is required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Authenticated user not found"));
    }
}
