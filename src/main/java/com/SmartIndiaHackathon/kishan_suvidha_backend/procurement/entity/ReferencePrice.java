package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity;


import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "reference_prices",
        indexes = {
                @Index(name = "idx_reference_price_crop_name", columnList = "crop_name"),
                @Index(name = "idx_reference_price_state", columnList = "state"),
                @Index(name = "idx_reference_price_effective_from", columnList = "effective_from")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferencePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "crop_name",
            nullable = false,
            length = 100
    )
    private String cropName;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "crop_type",
            nullable = false,
            length = 30
    )
    private CropType cropType;

    @Column(
            name = "price_per_quintal",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal pricePerQuintal;

    @Column(
            nullable = false,
            length = 100
    )
    private String state;

    @Column(
            name = "effective_from",
            nullable = false
    )
    private LocalDate effectiveFrom;

    @Column(
            name = "effective_to"
    )
    private LocalDate effectiveTo;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
