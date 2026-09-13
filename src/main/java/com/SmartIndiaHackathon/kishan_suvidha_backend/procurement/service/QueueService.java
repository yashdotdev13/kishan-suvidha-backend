package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service;


import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity.Crop;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.repository.CropRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Farmer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.FarmerRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.BadRequestException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.ResourceNotFoundException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.JoinQueueRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.QueueEntryResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.ProcurementCentre;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity.QueueEntry;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.enums.QueueStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository.ProcurementCentreRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.repository.QueueEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService {

    private static final double WAIT_MINUTES_PER_FARMER = 8.46;

    private final QueueEntryRepository queueEntryRepository;
    private final ProcurementCentreRepository procurementCentreRepository;
    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;

    @Transactional
    public QueueEntryResponse joinQueue(Long userId, JoinQueueRequest request) {
        Farmer farmer = farmerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Farmer profile not found"));

        Crop crop = cropRepository.findById(request.cropId()).orElseThrow(() ->
                new ResourceNotFoundException("Crop not found"));

        if (!crop.getFarmer().getId().equals(farmer.getId())) {
            throw new BadRequestException("You can only queue your own crop");
        }

        if (crop.getStatus() != CropStatus.AVAILABLE) {
            throw new BadRequestException("Only available crops can join the procurement queue");
        }

        boolean alreadyQueued = queueEntryRepository.existsByFarmerIdAndCropIdAndStatus(farmer.getId(), crop.getId(),
                QueueStatus.WAITING);

        if (alreadyQueued) {
            throw new BadRequestException("This crop is already in the procurement queue");
        }
        ProcurementCentre centre = procurementCentreRepository.findByIdForUpdate(request
                .procurementCentreId()).orElseThrow(() -> new ResourceNotFoundException("Procurement centre not found"));

        if (!centre.getAcceptingQueue()) {
            throw new BadRequestException("This procurement centre is not accepting new queue entries");
        }

        int nextToken = centre.getLastIssuedToken() + 1;
        centre.setLastIssuedToken(nextToken);

        QueueEntry queueEntry = QueueEntry.builder().farmer(farmer).crop(crop).procurementCentre(centre)
                .tokenNumber(nextToken).status(QueueStatus.WAITING).build();
        QueueEntry savedEntry = queueEntryRepository.save(queueEntry);
        return toResponse(savedEntry, centre);
    }


    @Transactional(readOnly = true)
    public List<QueueEntryResponse> getMyQueues(Long userId) {

        Farmer farmer = farmerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Farmer profile not found"));

        return queueEntryRepository.findByFarmerIdAndStatusIn(farmer.getId(),
                List.of(QueueStatus.WAITING, QueueStatus.SERVING))
                .stream().map(entry -> toResponse(entry, entry.getProcurementCentre())).toList();
    }

    @Transactional(readOnly = true)
    public List<QueueEntryResponse> getCentreQueue(Long centreId) {

        ProcurementCentre centre = procurementCentreRepository.findById(centreId).orElseThrow(() ->
                new ResourceNotFoundException("Procurement centre not found"));
        return queueEntryRepository.findByProcurementCentreIdAndStatusOrderByTokenNumberAsc(centreId,
                QueueStatus.WAITING).stream().map(entry -> toResponse(entry, centre)).toList();
    }


    @Transactional
    public QueueEntryResponse callNextFarmer(Long centreId) {
        ProcurementCentre centre = procurementCentreRepository.findByIdForUpdate(centreId).orElseThrow(() ->
                new ResourceNotFoundException("Procurement centre not found"));

        if (!Boolean.TRUE.equals(centre.getAcceptingQueue())) {
            throw new BadRequestException("This procurement centre is not accepting queue operations");
        }

        boolean farmerCurrentlyServing = queueEntryRepository.findFirstByProcurementCentreIdAndStatus(centreId,
                QueueStatus.SERVING).isPresent();
        if (farmerCurrentlyServing) {
            throw new BadRequestException("A farmer is already being served");
        }

        QueueEntry nextEntry = queueEntryRepository.findFirstByProcurementCentreIdAndStatusOrderByTokenNumberAsc(centreId,
                QueueStatus.WAITING).orElseThrow(() -> new ResourceNotFoundException("No farmers are waiting in the queue"));

        nextEntry.setStatus(QueueStatus.SERVING);
        nextEntry.setServedAt(LocalDateTime.now());

        centre.setCurrentToken(nextEntry.getTokenNumber());

        QueueEntry savedEntry = queueEntryRepository.save(nextEntry);

        return toResponse(savedEntry, centre);
    }

    @Transactional
    public QueueEntryResponse completeQueueEntry(Long queueEntryId) {

        QueueEntry queueEntry = queueEntryRepository.findById(queueEntryId).orElseThrow(() ->
                new ResourceNotFoundException("Queue entry not found"));

        if (queueEntry.getStatus() != QueueStatus.SERVING) {
            throw new BadRequestException("Only a farmer currently being served can be completed");
        }

        queueEntry.setStatus(QueueStatus.COMPLETED);
        queueEntry.setCompletedAt(LocalDateTime.now());

        QueueEntry savedEntry = queueEntryRepository.save(queueEntry);

        return toResponse(savedEntry, queueEntry.getProcurementCentre());
    }

    @Transactional
    public QueueEntryResponse cancelQueueEntry(Long userId, Long queueEntryId) {

        Farmer farmer = farmerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Farmer profile not found"));

        QueueEntry queueEntry = queueEntryRepository.findById(queueEntryId).orElseThrow(() ->
                new ResourceNotFoundException("Queue entry not found"));
        if (!queueEntry.getFarmer().getId().equals(farmer.getId())) {
            throw new BadRequestException("You can only cancel your own queue entry");
        }

        if (queueEntry.getStatus() != QueueStatus.WAITING) {
            throw new BadRequestException("Only a waiting queue entry can be cancelled");
        }

        queueEntry.setStatus(QueueStatus.CANCELLED);
        queueEntry.setCancelledAt(LocalDateTime.now());

        QueueEntry savedEntry = queueEntryRepository.save(queueEntry);

        return toResponse(savedEntry, queueEntry.getProcurementCentre());
    }

    private QueueEntryResponse toResponse(QueueEntry entry, ProcurementCentre centre) {
        int farmersAhead = Math.max(0, entry.getTokenNumber() - centre.getCurrentToken());
        int estimatedWaitMinutes = (int) Math.round(farmersAhead * WAIT_MINUTES_PER_FARMER);
        return new QueueEntryResponse(entry.getId(), entry.getCrop().getId(),
                centre.getId(), centre.getName(), entry.getTokenNumber(),
                centre.getCurrentToken(), farmersAhead, estimatedWaitMinutes,
                entry.getStatus(), entry.getJoinedAt(), entry.getServedAt(),
                entry.getCompletedAt(), entry.getCancelledAt());
    }
}