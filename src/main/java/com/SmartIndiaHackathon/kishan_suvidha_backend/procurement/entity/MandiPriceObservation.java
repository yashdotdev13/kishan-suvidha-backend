
package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "mandi_price_observations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mandi_price_source_record",
                        columnNames = {"source", "source_record_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_mandi_price_crop_market_date",
                        columnList = "crop_name, market_name, observation_date"
                ),
                @Index(
                        name = "idx_mandi_price_state_date",
                        columnList = "state, observation_date"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MandiPriceObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crop_name", nullable = false, length = 100)
    private String cropName;

    @Column(name = "market_name", nullable = false, length = 150)
    private String marketName;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(name = "observation_date", nullable = false)
    private LocalDate observationDate;

    @Column(name = "min_price_per_quintal", precision = 12, scale = 2)
    private BigDecimal minPricePerQuintal;

    @Column(name = "modal_price_per_quintal", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal modalPricePerQuintal;

    @Column(name = "max_price_per_quintal", precision = 12, scale = 2)
    private BigDecimal maxPricePerQuintal;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(name = "source_record_id", nullable = false, length = 200)
    private String sourceRecordId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
