package com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.controller;

import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos.CreateTransportOptionRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos.TransportOptionResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.service.TransportOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transport-options")
@RequiredArgsConstructor
public class TransportOptionController {

    private final TransportOptionService transportOptionService;

    @PostMapping
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<TransportOptionResponse> create(
            @Valid @RequestBody CreateTransportOptionRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transportOptionService.create(request));
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('FARMER', 'BUYER', 'OFFICER')")
    public ResponseEntity<List<TransportOptionResponse>> getAvailable() {

        return ResponseEntity.ok(
                transportOptionService.getAvailable()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FARMER', 'BUYER', 'OFFICER')")
    public ResponseEntity<TransportOptionResponse> getById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                transportOptionService.getById(id)
        );
    }
}