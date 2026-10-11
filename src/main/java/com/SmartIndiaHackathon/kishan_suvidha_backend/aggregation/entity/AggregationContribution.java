 package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity.Crop;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationContributionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "aggregation_contributions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_aggregation_plan_crop",
                        columnNames = {"plan_id", "crop_id"}
                )
        },
        indexes = {
                @Index(name = "idx_aggregation_contribution_plan", columnList = "plan_id"),
                @Index(name = "idx_aggregation_contribution_crop", columnList = "crop_id"),
                @Index(name = "idx_aggregation_contribution_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregationContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private AggregationPlan plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    /**
     * Proposed contribution quantity, always normalized to kilograms.
     */
    @Column(name = "proposed_quantity_kg", nullable = false, precision = 15, scale = 3)
    private BigDecimal proposedQuantityKg;

    /**
     * Quantity actually reserved after the farmer accepts and the system
     * revalidates available stock.
     */
    @Column(name = "reserved_quantity_kg", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal reservedQuantityKg = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AggregationContributionStatus status =
            AggregationContributionStatus.PROPOSED;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

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
            status = AggregationContributionStatus.PROPOSED;
        }

        if (reservedQuantityKg == null) {
            reservedQuantityKg = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}