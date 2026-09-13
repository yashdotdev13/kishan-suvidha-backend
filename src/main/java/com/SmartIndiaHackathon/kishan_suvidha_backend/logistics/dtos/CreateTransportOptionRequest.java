package com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.dtos;

import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.enums.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateTransportOptionRequest(

        @NotNull(message = "Vehicle type is required")
        VehicleType vehicleType,

        @NotBlank(message = "Vehicle number is required")
        String vehicleNumber,

        @NotNull(message = "Vehicle capacity is required")
        @DecimalMin(
                value = "0.001",
                message = "Vehicle capacity must be greater than zero"
        )
        BigDecimal capacityQuintals,

        @NotNull(message = "Transport rate is required")
        @DecimalMin(
                value = "0.01",
                message = "Transport rate must be greater than zero"
        )
        BigDecimal ratePerKmPerQuintal
) {
}