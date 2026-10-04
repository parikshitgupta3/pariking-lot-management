package com.rapidstack.pariking_lot_management.application.usecase.exit;

import com.rapidstack.pariking_lot_management.application.exception.TicketNotFoundException;
import com.rapidstack.pariking_lot_management.application.port.ParkingTicketRepository;
import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.TicketStatus;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import com.rapidstack.pariking_lot_management.domain.exception.TicketAlreadyCompletedException;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;
import com.rapidstack.pariking_lot_management.domain.strategy.pricing.ParkingFeeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingExitServiceTest {

    private static final Instant ENTRY_INSTANT = Instant.parse("2026-10-04T10:00:00Z");
    private static final Instant EXIT_INSTANT = Instant.parse("2026-10-04T11:30:00Z");
    private static final BigDecimal EXPECTED_FEE = new BigDecimal("15.00");

    @Mock
    private ParkingTicketRepository ticketRepository;

    @Mock
    private ParkingFeeStrategy feeStrategy;

    private final Vehicle car = new Vehicle("AB12CD3456", VehicleType.CAR);

    private ParkingExitService service;

    @BeforeEach
    void setUp() {
        service = new ParkingExitService(ticketRepository, feeStrategy, Clock.fixed(EXIT_INSTANT, ZoneOffset.UTC));
    }

    @Test
    void checkoutCompletesTicketReleasesSpotAndRecordsFee() {
        ParkingTicket ticket = activeTicketOnOccupiedSpot();
        when(ticketRepository.findById("t1")).thenReturn(Optional.of(ticket));
        when(feeStrategy.calculateFee(same(VehicleType.CAR), eq(Duration.ofMinutes(90))))
                .thenReturn(EXPECTED_FEE);

        ParkingTicket completed = service.checkout("t1");

        assertEquals(TicketStatus.COMPLETED, completed.getStatus());
        assertEquals(EXIT_INSTANT, completed.getExitTime());
        assertEquals(0, EXPECTED_FEE.compareTo(completed.getFee()));
        assertEquals(SpotStatus.AVAILABLE, ticket.getSpot().getStatus());
        verify(ticketRepository).save(same(ticket));
    }

    @Test
    void checkoutRejectsUnknownTicketId() {
        when(ticketRepository.findById("missing")).thenReturn(Optional.empty());

        TicketNotFoundException exception = assertThrows(TicketNotFoundException.class,
                () -> service.checkout("missing"));

        assertTrue(exception.getMessage().contains("missing"));
        verifyNoInteractions(feeStrategy);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void checkoutRejectsAlreadyCompletedTicket() {
        ParkingTicket completedTicket = completedTicket();
        when(ticketRepository.findById("t1")).thenReturn(Optional.of(completedTicket));

        TicketAlreadyCompletedException exception = assertThrows(TicketAlreadyCompletedException.class,
                () -> service.checkout("t1"));

        assertTrue(exception.getMessage().contains("t1"));
        assertEquals(SpotStatus.OCCUPIED, completedTicket.getSpot().getStatus());
        verifyNoInteractions(feeStrategy);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void checkoutRejectsNullAndBlankTicketId() {
        assertThrows(NullPointerException.class, () -> service.checkout(null));
        assertThrows(IllegalArgumentException.class, () -> service.checkout("  "));
    }

    @Test
    void checkoutPropagatesSpotReleaseGuardWhenSpotIsNotOccupied() {
        ParkingSpot driftedSpot = new ParkingSpot("s1", "A-1", SpotType.COMPACT, SpotStatus.AVAILABLE);
        ParkingTicket ticket = new ParkingTicket("t1", car, driftedSpot, ENTRY_INSTANT);
        when(ticketRepository.findById("t1")).thenReturn(Optional.of(ticket));
        when(feeStrategy.calculateFee(same(VehicleType.CAR), any())).thenReturn(EXPECTED_FEE);

        assertThrows(IllegalStateException.class, () -> service.checkout("t1"));

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void rejectsNullConstructorDependencies() {
        Clock clock = Clock.fixed(EXIT_INSTANT, ZoneOffset.UTC);

        assertThrows(NullPointerException.class,
                () -> new ParkingExitService(null, feeStrategy, clock));
        assertThrows(NullPointerException.class,
                () -> new ParkingExitService(ticketRepository, null, clock));
        assertThrows(NullPointerException.class,
                () -> new ParkingExitService(ticketRepository, feeStrategy, null));
    }

    private ParkingTicket activeTicketOnOccupiedSpot() {
        ParkingSpot spot = new ParkingSpot("s1", "A-1", SpotType.COMPACT, SpotStatus.OCCUPIED);
        return new ParkingTicket("t1", car, spot, ENTRY_INSTANT);
    }

    private ParkingTicket completedTicket() {
        ParkingSpot spot = new ParkingSpot("s1", "A-1", SpotType.COMPACT, SpotStatus.OCCUPIED);
        return new ParkingTicket("t1", car, spot, ENTRY_INSTANT, EXIT_INSTANT, EXPECTED_FEE,
                TicketStatus.COMPLETED);
    }
}
