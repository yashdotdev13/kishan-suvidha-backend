package com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class HaversineDistanceService implements DistanceService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public BigDecimal calculateDistanceKm(
            Double sourceLatitude,
            Double sourceLongitude,
            Double destinationLatitude,
            Double destinationLongitude
    ) {

        validateCoordinates(
                sourceLatitude,
                sourceLongitude,
                destinationLatitude,
                destinationLongitude
        );

        double lat1 = Math.toRadians(sourceLatitude);
        double lon1 = Math.toRadians(sourceLongitude);

        double lat2 = Math.toRadians(destinationLatitude);
        double lon2 = Math.toRadians(destinationLongitude);

        double deltaLatitude = lat2 - lat1;
        double deltaLongitude = lon2 - lon1;

        double a =
                Math.sin(deltaLatitude / 2)
                        * Math.sin(deltaLatitude / 2)
                        +
                        Math.cos(lat1)
                                * Math.cos(lat2)
                                * Math.sin(deltaLongitude / 2)
                                * Math.sin(deltaLongitude / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        double distance = EARTH_RADIUS_KM * c;

        return BigDecimal.valueOf(distance)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateCoordinates(
            Double sourceLatitude,
            Double sourceLongitude,
            Double destinationLatitude,
            Double destinationLongitude
    ) {

        if (sourceLatitude == null
                || sourceLongitude == null
                || destinationLatitude == null
                || destinationLongitude == null) {

            throw new IllegalArgumentException(
                    "Source and destination coordinates are required"
            );
        }

        if (sourceLatitude < -90 || sourceLatitude > 90
                || destinationLatitude < -90
                || destinationLatitude > 90) {

            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90"
            );
        }

        if (sourceLongitude < -180 || sourceLongitude > 180
                || destinationLongitude < -180
                || destinationLongitude > 180) {

            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180"
            );
        }
    }
}