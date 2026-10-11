package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.dtos.CreateAggregationDemandRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationDemand;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationDemandStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationDemandRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Buyer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.User;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.BuyerRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AggregationDemandService {

    private final AggregationDemandRepository demandRepository;
    private final BuyerRepository buyerRepository;
    private final UserRepository userRepository;

    @Transactional
    public AggregationDemand createDemand(
            Long buyerId,
            CreateAggregationDemandRequest request
    ) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Buyer profile not found"));

        validateRequest(request);

        AggregationDemand demand = AggregationDemand.builder()
                .buyer(buyer)
                .cropName(request.cropName().trim())
                .cropType(request.cropType())
                .targetQuantityKg(request.targetQuantityKg())
                .maxPricePerKg(request.maxPricePerKg())
                .deliveryLocation(request.deliveryLocation().trim())
                .deliveryLatitude(request.deliveryLatitude())
                .deliveryLongitude(request.deliveryLongitude())
                .requiredBy(request.requiredBy())
                .status(AggregationDemandStatus.OPEN)
                .build();

        return demandRepository.save(demand);
    }

    @Transactional(readOnly = true)
    public Page<AggregationDemand> getBuyerDemands(
            Long buyerId,
            Pageable pageable
    ) {
        return demandRepository.findByBuyerId(buyerId, pageable);
    }

    @Transactional(readOnly = true)
    public AggregationDemand getDemand(Long demandId) {
        return demandRepository.findById(demandId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Aggregation demand not found"));
    }

    private void validateRequest(CreateAggregationDemandRequest request) {
        if (request.targetQuantityKg() == null
                || request.targetQuantityKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Target quantity must be greater than zero");
        }

        if (request.maxPricePerKg() != null
                && request.maxPricePerKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Maximum price per kilogram must be greater than zero");
        }
    }

    @Transactional(readOnly = true)
    public Long getAuthenticatedBuyerId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new AccessDeniedException("Authentication is required");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new AccessDeniedException("Authenticated user not found"));

        if (user.getRole() == null || !user.getRole().name().equals("BUYER")) {
            throw new AccessDeniedException("Only buyers can manage aggregation demands");
        }

        return buyerRepository.findByUserId(user.getId())
                .map(Buyer::getId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Buyer profile not found"));
    }
}
