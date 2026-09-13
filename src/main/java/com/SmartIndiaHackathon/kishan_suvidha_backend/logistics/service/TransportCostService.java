package com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Buyer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.BadRequestException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.ResourceNotFoundException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos.TransportCostEstimateResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.entity.TransportOption;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.enums.TransportRouteType;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.repository.TransportOptionRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.entity.Offer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.repository.OfferRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.ProcurementCentre;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository.ProcurementCentreRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Farmer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class TransportCostService {

    private final FarmerRepository farmerRepository;
    private final ProcurementCentreRepository procurementCentreRepository;
    private final TransportOptionRepository transportOptionRepository;
    private final DistanceService distanceService;
    private final OfferRepository offerRepository;

    @Transactional(readOnly = true)
    public TransportCostEstimateResponse estimateToProcurementCentre(
            Long userId,
            Long centreId,
            Long transportOptionId,
            BigDecimal quantity
    ) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Quantity must be greater than zero"
            );
        }

        Farmer farmer = farmerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Farmer profile not found"
                        )
                );

        ProcurementCentre centre =
                procurementCentreRepository.findById(centreId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Procurement centre not found"
                                )
                        );

        TransportOption transportOption =
                transportOptionRepository.findById(transportOptionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transport option not found"
                                )
                        );

        if (!Boolean.TRUE.equals(transportOption.getAvailable())) {
            throw new BadRequestException(
                    "Transport option is not currently available"
            );
        }

        if (transportOption.getCapacityQuintals()
                .compareTo(quantity) < 0) {

            throw new BadRequestException(
                    "Transport capacity is insufficient for this quantity"
            );
        }

        if (farmer.getLatitude() == null ||
                farmer.getLongitude() == null) {

            throw new BadRequestException(
                    "Farmer location coordinates are not available"
            );
        }

        if (centre.getLatitude() == null ||
                centre.getLongitude() == null) {

            throw new BadRequestException(
                    "Procurement centre coordinates are not available"
            );
        }

        BigDecimal distanceKm =
                distanceService.calculateDistanceKm(
                        farmer.getLatitude(),
                        farmer.getLongitude(),
                        centre.getLatitude(),
                        centre.getLongitude()
                );

        BigDecimal transportCost = distanceKm
                .multiply(transportOption.getRatePerKmPerQuintal())
                .multiply(quantity)
                .setScale(2, RoundingMode.HALF_UP);

        return new TransportCostEstimateResponse(
                TransportRouteType.PROCUREMENT_CENTRE,
                centre.getId(),
                transportOption.getId(),
                quantity,
                distanceKm,
                transportOption.getRatePerKmPerQuintal(),
                transportCost
        );
    }


    @Transactional(readOnly = true)
    public TransportCostEstimateResponse estimateForMarketplaceOffer(
            Long userId,
            Long offerId,
            Long transportOptionId
    ) {
        Farmer farmer = farmerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Farmer profile not found"));

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Offer not found"));

        if (!offer.getCrop().getFarmer().getId().equals(farmer.getId())) {
            throw new BadRequestException(
                    "You can only estimate transport cost for offers received for your crops"
            );
        }

        Buyer buyer = offer.getBuyer();

        if (farmer.getLatitude() == null || farmer.getLongitude() == null) {
            throw new BadRequestException(
                    "Farmer location coordinates are not available"
            );
        }

        if (buyer.getLatitude() == null || buyer.getLongitude() == null) {
            throw new BadRequestException(
                    "Buyer location coordinates are not available"
            );
        }

        TransportOption transportOption = transportOptionRepository
                .findById(transportOptionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transport option not found"
                        ));

        if (!transportOption.getAvailable()) {
            throw new BadRequestException(
                    "Transport option is not currently available"
            );
        }

        if (transportOption.getCapacityQuintals()
                .compareTo(offer.getQuantity()) < 0) {

            throw new BadRequestException(
                    "Transport capacity is insufficient for this offer quantity"
            );
        }

        BigDecimal distanceKm = distanceService.calculateDistanceKm(
                farmer.getLatitude(),
                farmer.getLongitude(),
                buyer.getLatitude(),
                buyer.getLongitude()
        );

        BigDecimal transportCost = distanceKm
                .multiply(transportOption.getRatePerKmPerQuintal())
                .multiply(offer.getQuantity())
                .setScale(2, RoundingMode.HALF_UP);

        return new TransportCostEstimateResponse(
                TransportRouteType.MARKETPLACE_BUYER,
                buyer.getId(),
                transportOption.getId(),
                offer.getQuantity(),
                distanceKm,
                transportOption.getRatePerKmPerQuintal(),
                transportCost
        );
    }
}