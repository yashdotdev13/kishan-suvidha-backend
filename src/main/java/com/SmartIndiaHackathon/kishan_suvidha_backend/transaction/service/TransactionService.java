package com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.service;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity.Crop;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.repository.CropRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Buyer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Farmer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.BuyerRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.FarmerRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.*;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos.AssignTransportRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.entity.TransportOption;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.repository.TransportOptionRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.service.DistanceService;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.entity.Offer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.enums.OfferStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.repository.OfferRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.dtos.SchedulePickupRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.dtos.TransactionResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.entity.Transaction;
import com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.enums.TransactionStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final OfferRepository offerRepository;
    private final CropRepository cropRepository;
    private final BuyerRepository buyerRepository;
    private final FarmerRepository farmerRepository;
    private final TransportOptionRepository transportOptionRepository;
    private final DistanceService distanceService;

    @Transactional
    public TransactionResponse createFromAcceptedOffer(Long userId, Long offerId) {

        Buyer buyer = buyerRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found"));

        Offer offer = offerRepository.findByIdForUpdate(offerId).orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        if (!offer.getBuyer().getId().equals(buyer.getId())) {
            throw new BadRequestException("You can only create transactions for your own offers");
        }

        if (offer.getStatus() != OfferStatus.ACCEPTED) {
            throw new BadRequestException("Only accepted offers can create transactions");
        }

        if (transactionRepository.existsByOfferId(offerId)) {
            throw new BadRequestException("A transaction already exists for this offer");
        }

        Crop crop = cropRepository.findByIdForUpdate(offer.getCrop().getId()).orElseThrow(() -> new ResourceNotFoundException("Crop not found"));

        if (crop.getStatus() != CropStatus.AVAILABLE) {
            throw new BadRequestException("This crop is no longer available");
        }

        if (offer.getQuantity().compareTo(crop.getQuantity()) > 0) {
            throw new BadRequestException("Offer quantity exceeds available crop quantity");
        }

        BigDecimal quantity = offer.getQuantity();

        BigDecimal agreedPrice;

        if (offer.getCounterPrice() != null) {
            agreedPrice = offer.getCounterPrice();
        } else {
            agreedPrice = offer.getOfferedPrice();
        }

        BigDecimal grossAmount = agreedPrice.multiply(quantity);
        BigDecimal transportCost = BigDecimal.ZERO;
        BigDecimal netAmount = grossAmount.subtract(transportCost);

        Transaction transaction = Transaction.builder().offer(offer).crop(crop).farmer(crop.
                getFarmer()).buyer(buyer).quantity(quantity).agreedPrice(agreedPrice).
                grossAmount(grossAmount).transportCost(transportCost).netAmount(netAmount).
                status(TransactionStatus.OFFER_ACCEPTED).build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        BigDecimal remainingQuantity = crop.getQuantity().subtract(quantity);
        crop.setQuantity(remainingQuantity);
        if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            crop.setStatus(CropStatus.SOLD);
        }

        cropRepository.save(crop);
        return toResponse(savedTransaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions(Long userId) {

        Buyer buyer = buyerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Buyer profile not found"));
        return transactionRepository.findByBuyerIdOrderByCreatedAtDesc(buyer
                .getId()).stream().map(this::toResponse).toList();
    }


    @Transactional(readOnly = true)
    public List<TransactionResponse> getMySales(Long userId) {

        Farmer farmer = farmerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Farmer profile not found"));
        return transactionRepository.findByFarmerIdOrderByCreatedAtDesc(farmer.getId()).
                stream().map(this::toResponse).toList();
    }


    @Transactional
    public TransactionResponse assignTransport(Long userId, Long transactionId, AssignTransportRequest request) {

        Transaction transaction = transactionRepository.findByIdForUpdate(transactionId).orElseThrow(() ->
                new ResourceNotFoundException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.OFFER_ACCEPTED) {
            throw new BadRequestException("Transport can only be assigned when transaction status is OFFER_ACCEPTED");
        }

        boolean isBuyer = transaction.getBuyer().getUser().getId().equals(userId);

        boolean isFarmer = transaction.getFarmer().getUser().getId().equals(userId);

        if (!isBuyer && !isFarmer) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "You are not authorized to assign transport for this transaction");
        }
        TransportOption transportOption = transportOptionRepository.findByIdForUpdate(request.transportOptionId()).orElseThrow(() -> new ResourceNotFoundException("Transport option not found"));
        if (!transportOption.getAvailable()) {
            throw new ConflictException(ErrorCode.CONFLICT, "Transport option is already assigned");
        }

        if (transportOption.getCapacityQuintals().compareTo(transaction.getQuantity()) < 0) {

            throw new BadRequestException("Transport capacity is insufficient for this transaction");
        }
        Farmer farmer = transaction.getFarmer();

        if (farmer.getLatitude() == null || farmer.getLongitude() == null) {
            throw new BadRequestException("Farmer location coordinates are not available");
        }
        Buyer buyer = transaction.getBuyer();

        if (buyer.getLatitude() == null || buyer.getLongitude() == null) {
            throw new BadRequestException("Buyer location coordinates are not available");
        }
        BigDecimal distanceKm = distanceService.calculateDistanceKm(farmer.getLatitude(),
                farmer.getLongitude(), buyer.getLatitude(), buyer.getLongitude());
        BigDecimal transportCost = distanceKm.multiply(transportOption.getRatePerKmPerQuintal()).
                multiply(transaction.getQuantity()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = transaction.getGrossAmount().subtract(transportCost).
                setScale(2, RoundingMode.HALF_UP);

        transaction.setTransportOption(transportOption);
        transaction.setDistanceKm(distanceKm);
        transaction.setTransportCost(transportCost);
        transaction.setNetAmount(netAmount);
        transaction.setStatus(TransactionStatus.TRANSPORT_ASSIGNED);

        transportOption.setAvailable(false);

        transactionRepository.save(transaction);
        transportOptionRepository.save(transportOption);

        return toResponse(transaction);
    }


    @Transactional
    public TransactionResponse schedulePickup(Long userId, Long transactionId, SchedulePickupRequest request) {

        Transaction transaction = transactionRepository.findByIdForUpdate(transactionId).orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND, "Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.TRANSPORT_ASSIGNED) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Pickup can only be scheduled after transport has been assigned");
        }
        if (request.pickupScheduledAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Pickup date and time must be in the future");
        }
        transaction.setPickupScheduledAt(request.pickupScheduledAt());

        transaction.setStatus(TransactionStatus.PICKUP_SCHEDULED);
        transactionRepository.save(transaction);

        return toResponse(transaction);
    }


    @Transactional
    public TransactionResponse markDelivered(Long userId, Long transactionId) {

        Transaction transaction = transactionRepository.findByIdForUpdate(transactionId).orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND, "Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.PICKUP_SCHEDULED) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Crop can only be marked as delivered after pickup has been scheduled");
        }
        transaction.setStatus(TransactionStatus.CROP_DELIVERED);
        transactionRepository.save(transaction);
        return toResponse(transaction);
    }

    private TransactionResponse toResponse(Transaction transaction) {

        return new TransactionResponse(transaction.getId(), transaction.getOffer().getId(), transaction.getCrop().getId(), transaction.getCrop().getCropName(), transaction.getFarmer().getId(), transaction.getBuyer().getId(),

                transaction.getTransportOption() != null ? transaction.getTransportOption().getId() : null,
                transaction.getTransportOption() != null ? transaction.getTransportOption().getVehicleNumber() : null,
                transaction.getQuantity(), transaction.getAgreedPrice(), transaction.getGrossAmount(),
                transaction.getDistanceKm(), transaction.getTransportCost(), transaction.getNetAmount(),
                transaction.getStatus(),
                transaction.getPickupScheduledAt(),
                transaction.getCreatedAt(), transaction.getUpdatedAt(), transaction.getCompletedAt(),
                transaction.getCancelledAt());
    }
}