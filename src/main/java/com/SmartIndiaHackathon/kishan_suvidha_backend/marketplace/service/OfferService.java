package com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity.Crop;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.repository.CropRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Buyer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Farmer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.BuyerRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.FarmerRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.BadRequestException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.ResourceNotFoundException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.dtos.CounterOfferRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.dtos.CreateOfferRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.dtos.OfferResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.entity.Offer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.enums.OfferStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final BuyerRepository buyerRepository;
    private final CropRepository cropRepository;
    private final FarmerRepository farmerRepository;

    @Transactional
    public OfferResponse createOffer(Long userId, CreateOfferRequest request) {

        Buyer buyer = buyerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Buyer profile not found"));

        Crop crop = cropRepository.findById(request.cropId()).orElseThrow(() ->
                new ResourceNotFoundException("Crop not found"));

        if (crop.getStatus() != CropStatus.AVAILABLE) {
            throw new BadRequestException("Offers can only be made for available crops");
        }
        if (request.quantity().compareTo(crop.getQuantity()) > 0) {
            throw new BadRequestException("Offer quantity cannot exceed available crop quantity");
        }
        boolean existingOffer = offerRepository.existsByCropIdAndBuyerIdAndStatus(crop.getId(), buyer.getId(), OfferStatus.PENDING);

        if (existingOffer) {
            throw new BadRequestException("You already have a pending offer for this crop");
        }
        Offer offer = Offer.builder().crop(crop).buyer(buyer).quantity(request.quantity())
                .offeredPrice(request.offeredPrice()).status(OfferStatus.PENDING).build();
        Offer savedOffer = offerRepository.save(offer);
        return toResponse(savedOffer);
    }


    @Transactional(readOnly = true)
    public List<OfferResponse> getMyReceivedOffers(Long userId) {

        Farmer farmer = farmerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Farmer profile not found"));

        return offerRepository.findByCropFarmerIdOrderByCreatedAtDesc(farmer.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public OfferResponse acceptOffer(Long userId, Long offerId) {

        Farmer farmer = farmerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Farmer profile not found"));
        Offer offer = offerRepository.findByIdForUpdate(offerId).orElseThrow(() ->
                new ResourceNotFoundException("Offer not found"));

        Crop crop = offer.getCrop();

        if (!crop.getFarmer().getId().equals(farmer.getId())) {
            throw new BadRequestException("You can only accept offers for your own crops");
        }

        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BadRequestException("Only pending offers can be accepted");
        }

        if (offer.getQuantity().compareTo(crop.getQuantity()) > 0) {
            throw new BadRequestException("Offer quantity is greater than the available crop quantity");
        }
        offer.setStatus(OfferStatus.ACCEPTED);
        Offer savedOffer = offerRepository.save(offer);
        return toResponse(savedOffer);
    }

    @Transactional
    public OfferResponse rejectOffer(Long userId, Long offerId) {
        Farmer farmer = farmerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Farmer profile not found"));

        Offer offer = offerRepository.findByIdForUpdate(offerId).orElseThrow(() ->
                new ResourceNotFoundException("Offer not found"));

        Crop crop = offer.getCrop();

        if (!crop.getFarmer().getId().equals(farmer.getId())) {
            throw new BadRequestException("You can only reject offers for your own crops");
        }
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BadRequestException("Only pending offers can be rejected");
        }
        offer.setStatus(OfferStatus.REJECTED);
        Offer savedOffer = offerRepository.save(offer);
        return toResponse(savedOffer);
    }


    @Transactional
    public OfferResponse counterOffer(Long userId, Long offerId, CounterOfferRequest request) {

        Farmer farmer = farmerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Farmer profile not found"));
        Offer offer = offerRepository.findByIdForUpdate(offerId).orElseThrow(() ->
                new ResourceNotFoundException("Offer not found"));
        Crop crop = offer.getCrop();

        if (!crop.getFarmer().getId().equals(farmer.getId())) {
            throw new BadRequestException("You can only counter offers for your own crops");
        }
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BadRequestException("Only pending offers can be countered");
        }

        offer.setCounterPrice(request.counterPrice());
        offer.setStatus(OfferStatus.COUNTERED);
        Offer savedOffer = offerRepository.save(offer);
        return toResponse(savedOffer);
    }


    @Transactional(readOnly = true)
    public List<OfferResponse> getMyOffers(Long userId) {
        Buyer buyer = buyerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Buyer profile not found"));
        return offerRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId()).stream().map(this::toResponse).toList();
    }


    @Transactional
    public OfferResponse acceptCounterOffer(Long userId, Long offerId) {

        Buyer buyer = buyerRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found"));
        Offer offer = offerRepository.findByIdForUpdate(offerId).orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        if (!offer.getBuyer().getId().equals(buyer.getId())) {
            throw new BadRequestException("You can only accept your own offers");
        }
        if (offer.getStatus() != OfferStatus.COUNTERED) {
            throw new BadRequestException("Only countered offers can be accepted");
        }
        if (offer.getCounterPrice() == null) {
            throw new BadRequestException("Counter price is not available");
        }
        offer.setStatus(OfferStatus.ACCEPTED);
        Offer savedOffer = offerRepository.save(offer);
        return toResponse(savedOffer);
    }


    @Transactional
    public OfferResponse rejectCounterOffer(Long userId, Long offerId) {

        Buyer buyer = buyerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Buyer profile not found"));

        Offer offer = offerRepository.findByIdForUpdate(offerId).orElseThrow(() ->
                new ResourceNotFoundException("Offer not found"));

        if (!offer.getBuyer().getId().equals(buyer.getId())) {
            throw new BadRequestException("You can only reject your own offers");
        }
        if (offer.getStatus() != OfferStatus.COUNTERED) {
            throw new BadRequestException("Only countered offers can be rejected");
        }
        offer.setStatus(OfferStatus.REJECTED);
        Offer savedOffer = offerRepository.save(offer);
        return toResponse(savedOffer);
    }

    private OfferResponse toResponse(Offer offer) {
        return new OfferResponse(offer.getId(), offer.getCrop().getId(),
                offer.getCrop().getCropName(), offer.getBuyer().getId(),
                offer.getQuantity(), offer.getOfferedPrice(), offer.getCounterPrice(),
                offer.getStatus(), offer.getCreatedAt(), offer.getUpdatedAt());
    }
}
