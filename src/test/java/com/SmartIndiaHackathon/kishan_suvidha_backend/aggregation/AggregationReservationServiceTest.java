
package com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation;

import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationContribution;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationDemand;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.entity.AggregationPlan;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationContributionStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.enums.AggregationPlanStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationContributionRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.repository.AggregationPlanRepository;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.service.AggregationReservationService;
import com.SmartIndiaHackathon.kishan_suvidha_backend.aggregation.service.CropQuantityConverter;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.entity.Crop;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropStatus;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.enums.CropUnit;
import com.SmartIndiaHackathon.kishan_suvidha_backend.agriculture.repository.CropRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AggregationReservationServiceTest {

    @Mock
    private AggregationPlanRepository planRepository;

    @Mock
    private AggregationContributionRepository contributionRepository;

    @Mock
    private CropRepository cropRepository;

    @Mock
    private CropQuantityConverter quantityConverter;

    @InjectMocks
    private AggregationReservationService reservationService;

    private AggregationPlan plan;

    @BeforeEach
    void setUp() {
        plan = mock(AggregationPlan.class);

        when(planRepository.findById(1L))
                .thenReturn(Optional.of(plan));
    }

    @Test
    void shouldRejectReservationWhenFarmerResponsesArePending() {
        when(plan.getStatus())
                .thenReturn(AggregationPlanStatus.AWAITING_FARMER_APPROVALS);

        assertThrows(
                IllegalStateException.class,
                () -> reservationService.reserveAcceptedContributions(1L)
        );

        verifyNoInteractions(contributionRepository, cropRepository);
    }

    @Test
    void shouldRejectReservationWhenPlanIsAlreadyReserved() {
        when(plan.getStatus())
                .thenReturn(AggregationPlanStatus.RESERVED);

        assertThrows(
                IllegalStateException.class,
                () -> reservationService.reserveAcceptedContributions(1L)
        );

        verifyNoInteractions(contributionRepository, cropRepository);
    }

    @Test
    void shouldRejectReservationWhenNoContributionsAreAccepted() {
        when(plan.getStatus())
                .thenReturn(AggregationPlanStatus.READY_TO_RESERVE);

        when(contributionRepository.findByPlanIdOrderByIdAsc(1L))
                .thenReturn(List.of());

        assertThrows(
                IllegalStateException.class,
                () -> reservationService.reserveAcceptedContributions(1L)
        );

        verifyNoInteractions(cropRepository);
    }

    @Test
    void shouldReserveAcceptedContributionAndUpdatePlan() {
        when(plan.getStatus())
                .thenReturn(AggregationPlanStatus.READY_TO_RESERVE);

        AggregationDemand demand = mock(AggregationDemand.class);
        when(plan.getDemand()).thenReturn(demand);
        when(demand.getTargetQuantityKg())
                .thenReturn(new BigDecimal("30"));

        AggregationContribution contribution =
                mock(AggregationContribution.class);
        Crop crop = mock(Crop.class);

        when(contribution.getStatus())
                .thenReturn(AggregationContributionStatus.ACCEPTED);
        when(contribution.getCrop()).thenReturn(crop);
        when(contribution.getProposedQuantityKg())
                .thenReturn(new BigDecimal("40"));

        when(crop.getId()).thenReturn(10L);
        when(crop.getStatus()).thenReturn(CropStatus.AVAILABLE);
        when(crop.getQuantity()).thenReturn(new BigDecimal("100"));
        when(crop.getUnit()).thenReturn(CropUnit.KG);

        when(contributionRepository.findByPlanIdOrderByIdAsc(1L))
                .thenReturn(List.of(contribution));

        when(cropRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(crop));

        when(quantityConverter.toKilograms(
                any(BigDecimal.class), eq(CropUnit.KG)))
                .thenReturn(
                        new BigDecimal("100"),
                        new BigDecimal("60.000")
                );

        when(planRepository.save(plan)).thenReturn(plan);

        AggregationPlan result =
                reservationService.reserveAcceptedContributions(1L);

        verify(crop).setQuantity(argThat(
                quantity -> quantity != null
                        && quantity.compareTo(new BigDecimal("60")) == 0
        ));

        verify(contribution).setReservedQuantityKg(argThat(
                quantity -> quantity != null
                        && quantity.compareTo(new BigDecimal("40")) == 0
        ));

        verify(contribution).setStatus(
                AggregationContributionStatus.RESERVED
        );

        verify(contributionRepository).saveAll(List.of(contribution));

        verify(plan).setPlannedQuantityKg(argThat(
                quantity -> quantity != null
                        && quantity.compareTo(new BigDecimal("40")) == 0
        ));

        verify(plan).setStatus(AggregationPlanStatus.RESERVED);

        assertEquals(plan, result);
    }

    @Test
    void shouldRejectReservationWhenCropIsUnavailable() {
        when(plan.getStatus())
                .thenReturn(AggregationPlanStatus.READY_TO_RESERVE);

        AggregationContribution contribution =
                mock(AggregationContribution.class);
        Crop crop = mock(Crop.class);

        when(contribution.getStatus())
                .thenReturn(AggregationContributionStatus.ACCEPTED);
        when(contribution.getCrop()).thenReturn(crop);
        when(contribution.getProposedQuantityKg())
                .thenReturn(new BigDecimal("40"));

        when(crop.getId()).thenReturn(10L);
        when(crop.getStatus()).thenReturn(CropStatus.CANCELLED);

        when(contributionRepository.findByPlanIdOrderByIdAsc(1L))
                .thenReturn(List.of(contribution));

        when(cropRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(crop));

        assertThrows(
                IllegalStateException.class,
                () -> reservationService.reserveAcceptedContributions(1L)
        );

        verify(contribution).setStatus(
                AggregationContributionStatus.RELEASED
        );

        verify(crop, never()).setQuantity(any(BigDecimal.class));
        verify(planRepository, never()).save(any(AggregationPlan.class));
    }
}
