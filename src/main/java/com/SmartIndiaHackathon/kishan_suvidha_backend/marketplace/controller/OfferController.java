package com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.controller;

import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.dtos.CounterOfferRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.dtos.CreateOfferRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.dtos.OfferResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<OfferResponse> createOffer(
            @Valid @RequestBody CreateOfferRequest request,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        OfferResponse response =
                offerService.createOffer(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/received")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<List<OfferResponse>> getMyReceivedOffers(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                offerService.getMyReceivedOffers(userId)
        );
    }


    @PostMapping("/{offerId}/accept")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<OfferResponse> acceptOffer(
            @PathVariable Long offerId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        OfferResponse response =
                offerService.acceptOffer(userId, offerId);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{offerId}/reject")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<OfferResponse> rejectOffer(
            @PathVariable Long offerId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        OfferResponse response =
                offerService.rejectOffer(userId, offerId);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{offerId}/counter")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<OfferResponse> counterOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody CounterOfferRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        OfferResponse response =
                offerService.counterOffer(
                        userId,
                        offerId,
                        request
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/my")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<List<OfferResponse>> getMyOffers(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                offerService.getMyOffers(userId)
        );
    }


    @PostMapping("/{offerId}/accept-counter")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<OfferResponse> acceptCounterOffer(
            @PathVariable Long offerId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        OfferResponse response =
                offerService.acceptCounterOffer(userId, offerId);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{offerId}/reject-counter")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<OfferResponse> rejectCounterOffer(
            @PathVariable Long offerId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        OfferResponse response =
                offerService.rejectCounterOffer(userId, offerId);

        return ResponseEntity.ok(response);
    }
}