package com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.controller;


import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.dtos.CreateCropRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.dtos.CropResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.service.CropService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crops")
@RequiredArgsConstructor
public class CropController {

    private final CropService cropService;

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<CropResponse> createCrop(Authentication authentication, @Valid @RequestBody CreateCropRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        CropResponse response = cropService.createCrop(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<List<CropResponse>> getMyCrops(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(cropService.getMyCrops(userId));
    }

    @GetMapping("/{cropId}")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<CropResponse> getMyCrop(Authentication authentication, @PathVariable Long cropId) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(cropService.getMyCrop(userId, cropId));
    }
}
