package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.CreateMandiPriceObservationRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.MandiPriceObservation;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository.MandiPriceObservationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MandiPriceObservationService {

    private final MandiPriceObservationRepository repository;

    public MandiPriceObservationService(
            MandiPriceObservationRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public MandiPriceObservation create(
            CreateMandiPriceObservationRequest request
    ) {
        validatePrices(request);

        if (repository.existsBySourceAndSourceRecordId(
                request.source().trim(),
                request.sourceRecordId().trim()
        )) {
            throw new IllegalArgumentException(
                    "A price observation with this source record ID already exists"
            );
        }

        MandiPriceObservation observation = MandiPriceObservation.builder()
                .cropName(request.cropName().trim())
                .marketName(request.marketName().trim())
                .state(request.state().trim())
                .observationDate(request.observationDate())
                .minPricePerQuintal(request.minPricePerQuintal())
                .modalPricePerQuintal(request.modalPricePerQuintal())
                .maxPricePerQuintal(request.maxPricePerQuintal())
                .source(request.source().trim())
                .sourceRecordId(request.sourceRecordId().trim())
                .build();

        try {
            return repository.saveAndFlush(observation);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(
                    "This source record has already been imported",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public List<MandiPriceObservation> getHistoricalPrices(
            String cropName,
            String state,
            LocalDate from,
            LocalDate to
    ) {
        validateDateRange(from, to);

        if (cropName == null || cropName.isBlank()) {
            throw new IllegalArgumentException("Crop name is required");
        }

        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("State is required");
        }

        return repository
                .findByCropNameIgnoreCaseAndStateIgnoreCaseAndObservationDateBetweenOrderByObservationDateAsc(
                        cropName.trim(),
                        state.trim(),
                        from,
                        to
                );
    }

    @Transactional(readOnly = true)
    public List<MandiPriceObservation> getMarketHistoricalPrices(
            String cropName,
            String marketName,
            String state,
            LocalDate from,
            LocalDate to
    ) {
        validateDateRange(from, to);

        if (cropName == null || cropName.isBlank()
                || marketName == null || marketName.isBlank()
                || state == null || state.isBlank()) {
            throw new IllegalArgumentException(
                    "Crop name, market name and state are required"
            );
        }

        return repository
                .findByCropNameIgnoreCaseAndMarketNameIgnoreCaseAndStateIgnoreCaseAndObservationDateBetweenOrderByObservationDateAsc(
                        cropName.trim(),
                        marketName.trim(),
                        state.trim(),
                        from,
                        to
                );
    }

    private void validatePrices(
            CreateMandiPriceObservationRequest request
    ) {
        BigDecimal min = request.minPricePerQuintal();
        BigDecimal modal = request.modalPricePerQuintal();
        BigDecimal max = request.maxPricePerQuintal();

        if (min != null && min.compareTo(modal) > 0) {
            throw new IllegalArgumentException(
                    "Minimum price cannot exceed modal price"
            );
        }

        if (max != null && modal.compareTo(max) > 0) {
            throw new IllegalArgumentException(
                    "Modal price cannot exceed maximum price"
            );
        }

        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException(
                    "Minimum price cannot exceed maximum price"
            );
        }

        if (request.source().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Source cannot exceed 100 characters"
            );
        }

        if (request.sourceRecordId().trim().length() > 200) {
            throw new IllegalArgumentException(
                    "Source record ID cannot exceed 200 characters"
            );
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required"
            );
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }

        if (to.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "End date cannot be in the future"
            );
        }
    }
}