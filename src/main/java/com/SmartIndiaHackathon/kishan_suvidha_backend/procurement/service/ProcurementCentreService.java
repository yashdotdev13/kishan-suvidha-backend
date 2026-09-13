package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service;


import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.ResourceNotFoundException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.CreateProcurementCentreRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.ProcurementCentreResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.ProcurementCentre;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository.ProcurementCentreRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.geocoding.Coordinates;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.geocoding.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcurementCentreService {

    private final ProcurementCentreRepository procurementCentreRepository;
    private final GeocodingService geocodingService;

    @Transactional
    public ProcurementCentreResponse createCentre(
            CreateProcurementCentreRequest request
    ) {

        String address = String.join(
                ", ",
                request.location(),
                request.district(),
                request.state(),
                "India"
        );

        Coordinates coordinates = geocodingService.geocode(address);

        ProcurementCentre centre = ProcurementCentre.builder()
                .name(request.name())
                .state(request.state())
                .district(request.district())
                .location(request.location())
                .latitude(coordinates.latitude())
                .longitude(coordinates.longitude())
                .capacity(request.capacity())
                .currentToken(0)
                .acceptingQueue(true)
                .build();

        ProcurementCentre savedCentre =
                procurementCentreRepository.save(centre);

        return toResponse(savedCentre);
    }

    @Transactional(readOnly = true)
    public List<ProcurementCentreResponse> getAllCentres() {
        return procurementCentreRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcurementCentreResponse getCentre(Long centreId) {
        ProcurementCentre centre = procurementCentreRepository.findById(centreId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Procurement centre not found"
                        )
                );

        return toResponse(centre);
    }

    @Transactional(readOnly = true)
    public List<ProcurementCentreResponse> getCentresByState(String state) {
        return procurementCentreRepository.findByState(state)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProcurementCentreResponse> getCentresByDistrict(String district) {
        return procurementCentreRepository.findByDistrict(district)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProcurementCentreResponse toResponse(
            ProcurementCentre centre
    ) {
        return new ProcurementCentreResponse(
                centre.getId(),
                centre.getName(),
                centre.getState(),
                centre.getDistrict(),
                centre.getLocation(),
                centre.getLatitude(),
                centre.getLongitude(),
                centre.getCapacity(),
                centre.getCurrentToken(),
                centre.getAcceptingQueue(),
                centre.getCreatedAt(),
                centre.getUpdatedAt()
        );
    }
}