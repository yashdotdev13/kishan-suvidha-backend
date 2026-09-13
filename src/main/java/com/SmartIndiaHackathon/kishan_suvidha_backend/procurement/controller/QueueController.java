package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.controller;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.JoinQueueRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.QueueEntryResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/queues")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/join")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<QueueEntryResponse> joinQueue(
            @Valid @RequestBody JoinQueueRequest request,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        QueueEntryResponse response =
                queueService.joinQueue(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<List<QueueEntryResponse>> getMyQueues(
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                queueService.getMyQueues(userId)
        );
    }

    @GetMapping("/centre/{centreId}")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<List<QueueEntryResponse>> getCentreQueue(
            @PathVariable Long centreId
    ) {

        return ResponseEntity.ok(
                queueService.getCentreQueue(centreId)
        );
    }
    @PostMapping("/centre/{centreId}/next")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<QueueEntryResponse> callNextFarmer(
            @PathVariable Long centreId
    ) {

        return ResponseEntity.ok(
                queueService.callNextFarmer(centreId)
        );
    }

    @PostMapping("/{queueEntryId}/complete")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<QueueEntryResponse> completeQueueEntry(
            @PathVariable Long queueEntryId
    ) {

        return ResponseEntity.ok(
                queueService.completeQueueEntry(queueEntryId)
        );
    }


    @PostMapping("/{queueEntryId}/cancel")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<QueueEntryResponse> cancelQueueEntry(
            @PathVariable Long queueEntryId,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                queueService.cancelQueueEntry(
                        userId,
                        queueEntryId
                )
        );
    }
}