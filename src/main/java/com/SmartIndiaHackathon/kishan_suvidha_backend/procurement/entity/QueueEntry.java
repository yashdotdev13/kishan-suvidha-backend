package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.entity;


import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity.Crop;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.Farmer;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.enums.QueueStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "queue_entries",
        indexes = {
                @Index(
                        name = "idx_queue_centre_token",
                        columnList = "procurement_centre_id, token_number"
                ),
                @Index(
                        name = "idx_queue_farmer",
                        columnList = "farmer_id"
                ),
                @Index(
                        name = "idx_queue_crop",
                        columnList = "crop_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "procurement_centre_id", nullable = false)
    private ProcurementCentre procurementCentre;

    @Column(name = "token_number", nullable = false)
    private Integer tokenNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private QueueStatus status = QueueStatus.WAITING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime servedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();

        if (status == null) {
            status = QueueStatus.WAITING;
        }
    }
}