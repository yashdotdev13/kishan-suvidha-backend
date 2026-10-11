package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationDemandStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropType;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Buyer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "aggregation_demands",
        indexes = {
                @Index(name = "idx_aggregation_demand_buyer", columnList = "buyer_id"),
                @Index(name = "idx_aggregation_demand_status", columnList = "status"),
                @Index(name = "idx_aggregation_demand_crop_type", columnList = "crop_type")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregationDemand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @Column(name = "crop_name", nullable = false, length = 100)
    private String cropName;

    @Enumerated(EnumType.STRING)
    @Column(name = "crop_type", nullable = false, length = 30)
    private CropType cropType;

    /**
     * Requested quantity normalized to kilograms.
     */
    @Column(name = "target_quantity_kg", nullable = false, precision = 15, scale = 3)
    private BigDecimal targetQuantityKg;

    /**
     * Optional maximum acceptable price per kilogram.
     */
    @Column(name = "max_price_per_kg", precision = 12, scale = 2)
    private BigDecimal maxPricePerKg;

    @Column(name = "delivery_location", nullable = false, length = 300)
    private String deliveryLocation;

    @Column(name = "delivery_latitude")
    private Double deliveryLatitude;

    @Column(name = "delivery_longitude")
    private Double deliveryLongitude;

    @Column(name = "required_by")
    private LocalDate requiredBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AggregationDemandStatus status = AggregationDemandStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = AggregationDemandStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
