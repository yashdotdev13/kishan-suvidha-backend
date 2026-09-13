package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.controller;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.CreateReferencePriceRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.ReferencePriceResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.ReferencePriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reference-prices")
@RequiredArgsConstructor
public class ReferencePriceController {

    private final ReferencePriceService referencePriceService;

    @PostMapping
    public ResponseEntity<ReferencePriceResponse> create(
            @Valid @RequestBody CreateReferencePriceRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(referencePriceService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReferencePriceResponse> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                referencePriceService.getById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<ReferencePriceResponse>> getByCropAndState(
            @RequestParam String cropName,
            @RequestParam String state
    ) {
        return ResponseEntity.ok(
                referencePriceService.getByCropAndState(
                        cropName,
                        state
                )
        );
    }

    @GetMapping("/current")
    public ResponseEntity<ReferencePriceResponse> getCurrentPrice(
            @RequestParam String cropName,
            @RequestParam String state
    ) {
        return ResponseEntity.ok(
                referencePriceService.getCurrentPrice(
                        cropName,
                        state
                )
        );
    }
}
