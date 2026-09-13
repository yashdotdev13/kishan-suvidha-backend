package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.controller;


import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.CreateProcurementCentreRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.ProcurementCentreResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.ProcurementCentreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procurement-centres")
@RequiredArgsConstructor
public class ProcurementCentreController {

    private final ProcurementCentreService procurementCentreService;


    @PostMapping
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<ProcurementCentreResponse> createCentre(
            @Valid @RequestBody CreateProcurementCentreRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(procurementCentreService.createCentre(request));
    }

    @GetMapping
    public ResponseEntity<List<ProcurementCentreResponse>> getAllCentres() {
        return ResponseEntity.ok(
                procurementCentreService.getAllCentres()
        );
    }

    @GetMapping("/{centreId}")
    public ResponseEntity<ProcurementCentreResponse> getCentre(
            @PathVariable Long centreId
    ) {
        return ResponseEntity.ok(
                procurementCentreService.getCentre(centreId)
        );
    }

    @GetMapping("/state/{state}")
    public ResponseEntity<List<ProcurementCentreResponse>> getByState(
            @PathVariable String state
    ) {
        return ResponseEntity.ok(
                procurementCentreService.getCentresByState(state)
        );
    }

    @GetMapping("/district/{district}")
    public ResponseEntity<List<ProcurementCentreResponse>> getByDistrict(
            @PathVariable String district
    ) {
        return ResponseEntity.ok(
                procurementCentreService.getCentresByDistrict(district)
        );
    }
}