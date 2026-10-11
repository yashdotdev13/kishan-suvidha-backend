package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationPlanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "aggregation_plans",
        indexes = {
                @Index(name = "idx_aggregation_plan_demand", columnList = "demand_id"),
                @Index(name = "idx_aggregation_plan_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregationPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demand_id", nullable = false)
    private AggregationDemand demand;

    @Column(name = "planned_quantity_kg", nullable = false, precision = 15, scale = 3)
    private BigDecimal plannedQuantityKg;

    @Column(name = "estimated_total_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedTotalCost;

    @Column(name = "estimated_transport_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedTransportCost;

    @Column(name = "estimated_distance_km", precision = 12, scale = 2)
    private BigDecimal estimatedDistanceKm;

    @Column(name = "algorithm_version", nullable = false, length = 50)
    @Builder.Default
    private String algorithmVersion = "v1";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    @Builder.Default
    private AggregationPlanStatus status = AggregationPlanStatus.PROPOSED;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = AggregationPlanStatus.PROPOSED;
        }

        if (algorithmVersion == null || algorithmVersion.isBlank()) {
            algorithmVersion = "v1";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
