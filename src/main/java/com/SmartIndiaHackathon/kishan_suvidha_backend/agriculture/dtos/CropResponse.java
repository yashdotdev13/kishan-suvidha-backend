package com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.dtos;



import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropType;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropUnit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CropResponse(

        Long id,

        Long farmerId,

        String cropName,

        CropType cropType,

        BigDecimal quantity,

        CropUnit unit,

        BigDecimal expectedPrice,

        LocalDate harvestDate,

        CropStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}