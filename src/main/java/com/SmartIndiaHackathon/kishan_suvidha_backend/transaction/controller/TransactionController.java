package com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.controller;

import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos.AssignTransportRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.dtos.SchedulePickupRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.dtos.TransactionResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/from-offer/{offerId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<TransactionResponse> createFromOffer(
            @PathVariable Long offerId,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        TransactionResponse response =
                transactionService.createFromAcceptedOffer(
                        userId,
                        offerId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<List<TransactionResponse>> getMyTransactions(
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                transactionService.getMyTransactions(userId)
        );
    }


    @GetMapping("/my-sales")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<List<TransactionResponse>> getMySales(
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                transactionService.getMySales(userId)
        );
    }


    @PostMapping("/{transactionId}/assign-transport")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<TransactionResponse> assignTransport(
            @PathVariable Long transactionId,
            @Valid @RequestBody AssignTransportRequest request,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                transactionService.assignTransport(
                        userId,
                        transactionId,
                        request
                )
        );
    }

    @PostMapping("/{transactionId}/schedule-pickup")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<TransactionResponse> schedulePickup(
            @PathVariable Long transactionId,
            @Valid @RequestBody SchedulePickupRequest request,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                transactionService.schedulePickup(
                        userId,
                        transactionId,
                        request
                )
        );
    }


    @PostMapping("/{transactionId}/mark-delivered")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<TransactionResponse> markDelivered(
            @PathVariable Long transactionId,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                transactionService.markDelivered(
                        userId,
                        transactionId
                )
        );
    }
}
