package com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.service;


import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.dtos.CreateCropRequest;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.dtos.CropResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity.Crop;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.repository.CropRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Farmer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.FarmerRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CropService {

    private final CropRepository cropRepository;
    private final FarmerRepository farmerRepository;

    @Transactional
    public CropResponse createCrop(Long userId, CreateCropRequest request) {

        Farmer farmer = farmerRepository.findByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("Farmer profile not found"));

        Crop crop = Crop.builder().farmer(farmer).cropName(request.cropName())
                .cropType(request.cropType()).quantity(request.quantity())
                .unit(request.unit()).expectedPrice(request.expectedPrice())
                .harvestDate(request.harvestDate())

                .status(CropStatus.AVAILABLE)

                .build();
        crop = cropRepository.save(crop);
        return toResponse(crop);
    }

    @Transactional(readOnly = true)
    public List<CropResponse> getMyCrops(Long userId) {
        Farmer farmer = farmerRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Farmer profile not found"));
        return cropRepository.findByFarmerId(farmer.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CropResponse getMyCrop(Long userId, Long cropId) {
        Farmer farmer = farmerRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Farmer profile not found"));
        Crop crop = cropRepository.findByIdAndFarmerId(cropId, farmer.getId()).orElseThrow(() ->
                new ResourceNotFoundException("Crop not found"));

        return toResponse(crop);
    }

    private CropResponse toResponse(Crop crop) {
        return new CropResponse(crop.getId(), crop.getFarmer().getId(),
                crop.getCropName(), crop.getCropType(), crop.getQuantity(),
                crop.getUnit(), crop.getExpectedPrice(), crop.getHarvestDate(),
                crop.getStatus(), crop.getCreatedAt(), crop.getUpdatedAt());
    }
}