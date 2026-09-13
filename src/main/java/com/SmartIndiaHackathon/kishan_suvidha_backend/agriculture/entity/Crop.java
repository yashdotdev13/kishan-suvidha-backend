package com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropType;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropUnit;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Farmer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "crops",
        indexes = {
                @Index(name = "idx_crop_farmer_id", columnList = "farmer_id"),
                @Index(name = "idx_crop_status", columnList = "status"),
                @Index(name = "idx_crop_name", columnList = "crop_name")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "farmer_id",
            nullable = false
    )
    private Farmer farmer;

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
            nullable = false,
            precision = 15,
            scale = 3
    )
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private CropUnit unit;

    @Column(
            name = "expected_price",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal expectedPrice;

    @Column(
            name = "harvest_date",
            nullable = false
    )
    private LocalDate harvestDate;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    @Builder.Default
    private CropStatus status = CropStatus.AVAILABLE;

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

        if (status == null) {
            status = CropStatus.AVAILABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}