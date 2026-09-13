package com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos;

import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.enums.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransportOptionResponse(

        Long id,

        VehicleType vehicleType,

        String vehicleNumber,

        BigDecimal capacityQuintals,

        BigDecimal ratePerKmPerQuintal,

        Boolean available,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}