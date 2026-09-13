package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "procurement_centres",
        indexes = {
                @Index(name = "idx_centre_district", columnList = "district"),
                @Index(name = "idx_centre_state", columnList = "state")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcurementCentre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(nullable = false, length = 200)
    private String location;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentToken = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer lastIssuedToken = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean acceptingQueue = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (currentToken == null) {
            currentToken = 0;
        }

        if (acceptingQueue == null) {
            acceptingQueue = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}