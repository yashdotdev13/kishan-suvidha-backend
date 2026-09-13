package com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.entity;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity.Crop;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Buyer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.enums.OfferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "offers",
        indexes = {
                @Index(name = "idx_offer_crop", columnList = "crop_id"),
                @Index(name = "idx_offer_buyer", columnList = "buyer_id"),
                @Index(name = "idx_offer_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal offeredPrice;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OfferStatus status = OfferStatus.PENDING;

    @Column(precision = 12, scale = 2)
    private BigDecimal counterPrice;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = OfferStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}