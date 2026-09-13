package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.BadRequestException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.ResourceNotFoundException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.CreateReferencePriceRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.ReferencePriceResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.ReferencePrice;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository.ReferencePriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferencePriceService {

    private final ReferencePriceRepository referencePriceRepository;

    @Transactional
    public ReferencePriceResponse create(CreateReferencePriceRequest request) {

        validateEffectiveDates(
                request.effectiveFrom(),
                request.effectiveTo()
        );

        ReferencePrice referencePrice = ReferencePrice.builder()
                .cropName(request.cropName().trim())
                .cropType(request.cropType())
                .pricePerQuintal(request.pricePerQuintal())
                .state(request.state().trim())
                .effectiveFrom(request.effectiveFrom())
                .effectiveTo(request.effectiveTo())
                .build();

        ReferencePrice saved = referencePriceRepository.save(referencePrice);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReferencePriceResponse getById(Long id) {

        ReferencePrice referencePrice = referencePriceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reference price not found"));

        return toResponse(referencePrice);
    }

    @Transactional(readOnly = true)
    public List<ReferencePriceResponse> getByCropAndState(
            String cropName,
            String state
    ) {

        return referencePriceRepository
                .findByCropNameIgnoreCaseAndStateIgnoreCase(
                        cropName,
                        state
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReferencePriceResponse getCurrentPrice(
            String cropName,
            String state
    ) {

        LocalDate today = LocalDate.now();

        return referencePriceRepository
                .findCurrentPrices(cropName, state, today)
                .stream()
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No reference price found for "
                                        + cropName
                                        + " in "
                                        + state
                        ));
    }

    private void validateEffectiveDates(
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (effectiveTo != null &&
                effectiveTo.isBefore(effectiveFrom)) {

            throw new BadRequestException(
                    "Effective to date cannot be before effective from date"
            );
        }
    }

    private ReferencePriceResponse toResponse(
            ReferencePrice referencePrice
    ) {

        return new ReferencePriceResponse(
                referencePrice.getId(),
                referencePrice.getCropName(),
                referencePrice.getCropType(),
                referencePrice.getPricePerQuintal(),
                referencePrice.getState(),
                referencePrice.getEffectiveFrom(),
                referencePrice.getEffectiveTo(),
                referencePrice.getCreatedAt(),
                referencePrice.getUpdatedAt()
        );
    }
}
