package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.service;


import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Buyer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Farmer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.BuyerRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.FarmerRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.common.exceptions.ResourceNotFoundException;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.geocoding.Coordinates;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.geocoding.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final FarmerRepository farmerRepository;
    private final BuyerRepository buyerRepository;
    private final GeocodingService geocodingService;

    @Transactional
    public void updateFarmerLocation(
            Long userId,
            String location
    ) {

        Farmer farmer = farmerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Farmer profile not found"
                        ));

        Coordinates coordinates =
                geocodingService.geocode(location);

        farmer.setLocation(location);
        farmer.setLatitude(coordinates.latitude());
        farmer.setLongitude(coordinates.longitude());

        farmerRepository.save(farmer);
    }

    @Transactional
    public void updateBuyerLocation(
            Long userId,
            String location
    ) {

        Buyer buyer = buyerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Buyer profile not found"
                        ));

        Coordinates coordinates =
                geocodingService.geocode(location);

        buyer.setLocation(location);
        buyer.setLatitude(coordinates.latitude());
        buyer.setLongitude(coordinates.longitude());

        buyerRepository.save(buyer);
    }
}