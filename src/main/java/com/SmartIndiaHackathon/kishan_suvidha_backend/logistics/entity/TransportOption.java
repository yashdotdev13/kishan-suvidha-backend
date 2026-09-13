package com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.entity;

import com.SmartIndiaHackathon.kishan_suvidha_backend.logistics.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transport_options",
        indexes = {
                @Index(name = "idx_transport_available", columnList = "available"),
                @Index(name = "idx_transport_vehicle_type", columnList = "vehicle_type")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleType vehicleType;

    @Column(nullable = false, length = 50)
    private String vehicleNumber;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal capacityQuintals;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal ratePerKmPerQuintal;

    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;

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

        if (available == null) {
            available = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}