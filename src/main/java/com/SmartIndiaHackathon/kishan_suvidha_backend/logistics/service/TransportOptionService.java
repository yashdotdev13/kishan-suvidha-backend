package com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.ResourceNotFoundException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos.CreateTransportOptionRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos.TransportOptionResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.entity.TransportOption;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.repository.TransportOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportOptionService {

    private final TransportOptionRepository transportOptionRepository;

    @Transactional
    public TransportOptionResponse create(
            CreateTransportOptionRequest request
    ) {

        TransportOption transportOption =
                TransportOption.builder()
                        .vehicleType(request.vehicleType())
                        .vehicleNumber(request.vehicleNumber())
                        .capacityQuintals(request.capacityQuintals())
                        .ratePerKmPerQuintal(
                                request.ratePerKmPerQuintal()
                        )
                        .available(true)
                        .build();

        TransportOption saved =
                transportOptionRepository.save(transportOption);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TransportOptionResponse> getAvailable() {

        return transportOptionRepository
                .findByAvailableTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransportOptionResponse getById(Long id) {

        TransportOption transportOption =
                transportOptionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transport option not found"
                                ));

        return toResponse(transportOption);
    }

    private TransportOptionResponse toResponse(
            TransportOption transportOption
    ) {

        return new TransportOptionResponse(
                transportOption.getId(),
                transportOption.getVehicleType(),
                transportOption.getVehicleNumber(),
                transportOption.getCapacityQuintals(),
                transportOption.getRatePerKmPerQuintal(),
                transportOption.getAvailable(),
                transportOption.getCreatedAt(),
                transportOption.getUpdatedAt()
        );
    }
}
