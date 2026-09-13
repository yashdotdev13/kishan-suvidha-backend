package com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.service;

import java.math.BigDecimal;

public interface DistanceService {

    BigDecimal calculateDistanceKm(
            Double sourceLatitude,
            Double sourceLongitude,
            Double destinationLatitude,
            Double destinationLongitude
    );
}