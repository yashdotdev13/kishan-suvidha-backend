package com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.entity;

import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity.Crop;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Buyer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Farmer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.entity.TransportOption;
import com.SmartIndiaHackathon.kishan_suvidha_backend.marketplace.entity.Offer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.transaction.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transaction_offer", columnList = "offer_id"),
                @Index(name = "idx_transaction_crop", columnList = "crop_id"),
                @Index(name = "idx_transaction_farmer", columnList = "farmer_id"),
                @Index(name = "idx_transaction_buyer", columnList = "buyer_id"),
                @Index(name = "idx_transaction_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "offer_id",
            nullable = false,
            unique = true
    )
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal agreedPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal transportCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TransactionStatus status =
            TransactionStatus.OFFER_ACCEPTED;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_option_id")
    private TransportOption transportOption;

    @Column(precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    @Column
    private LocalDateTime pickupScheduledAt;

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
            status = TransactionStatus.OFFER_ACCEPTED;
        }

        if (transportCost == null) {
            transportCost = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}