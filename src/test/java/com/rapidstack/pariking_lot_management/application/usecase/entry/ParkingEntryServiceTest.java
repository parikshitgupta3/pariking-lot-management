package com.rapidstack.pariking_lot_management.application.usecase.entry;

import com.rapidstack.pariking_lot_management.application.port.ParkingSpotRepository;
import com.rapidstack.pariking_lot_management.application.port.ParkingTicketRepository;
import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.TicketStatus;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import com.rapidstack.pariking_lot_management.domain.exception.NoAvailableSpotException;
import com.rapidstack.pariking_lot_management.domain.model.ParkingFloor;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;
import com.rapidstack.pariking_lot_management.domain.strategy.allocation.SpotAllocationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingEntryServiceTest {

    private static final Instant ENTRY_INSTANT = Instant.parse("2026-10-04T10:15:30Z");

    @Mock
    private SpotAllocationStrategy allocationStrategy;

    @Mock
    private ParkingSpotRepository spotRepository;

    @Mock
    private ParkingTicketRepository ticketRepository;

    @Captor
    private ArgumentCaptor<List<ParkingSpot>> candidatesCaptor;

    private final Vehicle car = new Vehicle("AB12CD3456", VehicleType.CAR);

    private ParkingEntryService service;

    @BeforeEach
    void setUp() {
        service = new ParkingEntryService(allocationStrategy, spotRepository, ticketRepository,
                Clock.fixed(ENTRY_INSTANT, ZoneOffset.UTC));
    }

    @Test
    void admitsVehicleOnAllocatedSpotAndOpensActiveTicket() {
        ParkingSpot spot = new ParkingSpot("s1", "A-1", SpotType.COMPACT);
        ParkingLot lot = singleFloorLot(spot);
        when(allocationStrategy.allocate(anyList(), same(car))).thenReturn(Optional.of(spot));
        when(spotRepository.findByIdForUpdate("s1")).thenReturn(Optional.of(spot));

        ParkingTicket ticket = service.admit(car, lot);

        assertNotNull(ticket.getId());
        assertFalse(ticket.getId().isBlank());
        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertEquals(car, ticket.getVehicle());
        assertEquals(spot, ticket.getSpot());
        assertEquals(ENTRY_INSTANT, ticket.getEntryTime());
        assertNull(ticket.getExitTime());
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
        verify(ticketRepository).save(same(ticket));
    }

    @Test
    void offersEverySpotOfEveryFloorAsCandidateInLotOrder() {
        ParkingSpot groundA = new ParkingSpot("s1", "A-1", SpotType.COMPACT);
        ParkingSpot groundB = new ParkingSpot("s2", "A-2", SpotType.BIKE);
        ParkingSpot firstFloorSpot = new ParkingSpot("s3", "B-1", SpotType.LARGE);
        ParkingLot lot = new ParkingLot("lot-1", "Rapid Lot", List.of(
                new ParkingFloor("f1", 1, List.of(groundA, groundB)),
                new ParkingFloor("f2", 2, List.of(firstFloorSpot))));
        when(allocationStrategy.allocate(anyList(), same(car))).thenReturn(Optional.empty());

        assertThrows(NoAvailableSpotException.class, () -> service.admit(car, lot));

        verify(allocationStrategy).allocate(candidatesCaptor.capture(), same(car));
        assertEquals(List.of(groundA, groundB, firstFloorSpot), candidatesCaptor.getValue());
        verifyNoInteractions(spotRepository);
        verify(ticketRepository, never()).save(any(ParkingTicket.class));
    }

    @Test
    void throwsNoAvailableSpotWhenStrategyFindsNothing() {
        ParkingSpot onlyBikeSpot = new ParkingSpot("s1", "A-1", SpotType.BIKE); // free but wrong size for a car
        ParkingLot lot = singleFloorLot(onlyBikeSpot);
        when(allocationStrategy.allocate(anyList(), same(car))).thenReturn(Optional.empty());

        NoAvailableSpotException exception = assertThrows(NoAvailableSpotException.class,
                () -> service.admit(car, lot));

        assertTrue(exception.getMessage().contains("AB12CD3456"));
        assertTrue(exception.getMessage().contains("Rapid Lot"));
        assertEquals(SpotStatus.AVAILABLE, onlyBikeSpot.getStatus()); // nothing mutated on failure
        verifyNoInteractions(spotRepository);
        verify(ticketRepository, never()).save(any(ParkingTicket.class));
    }

    @Test
    void skipsSpotTakenConcurrentlyAndAllocatesTheNextCandidate() {
        ParkingSpot firstChoice = new ParkingSpot("s1", "A-1", SpotType.COMPACT); // looks free in the snapshot
        ParkingSpot secondChoice = new ParkingSpot("s2", "A-2", SpotType.LARGE);
        ParkingLot lot = singleFloorLot(firstChoice, secondChoice);
        when(allocationStrategy.allocate(anyList(), same(car)))
                .thenReturn(Optional.of(firstChoice))
                .thenReturn(Optional.of(secondChoice));
        // The locked row for s1 reveals it was occupied by a concurrent entry.
        when(spotRepository.findByIdForUpdate("s1"))
                .thenReturn(Optional.of(new ParkingSpot("s1", "A-1", SpotType.COMPACT, SpotStatus.OCCUPIED)));
        when(spotRepository.findByIdForUpdate("s2")).thenReturn(Optional.of(secondChoice));

        ParkingTicket ticket = service.admit(car, lot);

        assertEquals("s2", ticket.getSpot().getId());
        assertEquals(SpotStatus.OCCUPIED, secondChoice.getStatus());
        assertEquals(SpotStatus.AVAILABLE, firstChoice.getStatus()); // snapshot copy never mutated
        verify(ticketRepository).save(same(ticket));

        verify(allocationStrategy, org.mockito.Mockito.times(2)).allocate(candidatesCaptor.capture(), same(car));
        List<List<ParkingSpot>> invocations = candidatesCaptor.getAllValues();
        assertEquals(List.of(firstChoice, secondChoice), invocations.get(0));
        assertEquals(List.of(secondChoice), invocations.get(1)); // first choice excluded on retry
    }

    @Test
    void skipsSpotThatVanishedFromTheDatabase() {
        ParkingSpot ghost = new ParkingSpot("s1", "A-1", SpotType.COMPACT);
        ParkingSpot survivor = new ParkingSpot("s2", "A-2", SpotType.LARGE);
        ParkingLot lot = singleFloorLot(ghost, survivor);
        when(allocationStrategy.allocate(anyList(), same(car)))
                .thenReturn(Optional.of(ghost))
                .thenReturn(Optional.of(survivor));
        when(spotRepository.findByIdForUpdate("s1")).thenReturn(Optional.empty());
        when(spotRepository.findByIdForUpdate("s2")).thenReturn(Optional.of(survivor));

        ParkingTicket ticket = service.admit(car, lot);

        assertEquals("s2", ticket.getSpot().getId());
        verify(ticketRepository).save(same(ticket));
    }

    @Test
    void surfacesNoAvailableSpotWhenTheOnlyCandidateWasTakenConcurrently() {
        ParkingSpot taken = new ParkingSpot("s1", "A-1", SpotType.COMPACT); // looks free in the snapshot
        ParkingLot lot = singleFloorLot(taken);
        when(allocationStrategy.allocate(anyList(), same(car))).thenReturn(Optional.of(taken));
        when(spotRepository.findByIdForUpdate("s1"))
                .thenReturn(Optional.of(new ParkingSpot("s1", "A-1", SpotType.COMPACT, SpotStatus.OCCUPIED)));

        assertThrows(NoAvailableSpotException.class, () -> service.admit(car, lot));

        assertEquals(SpotStatus.AVAILABLE, taken.getStatus());
        verify(ticketRepository, never()).save(any(ParkingTicket.class));
    }

    @Test
    void issuesDistinctTicketIdsPerAdmission() {
        ParkingSpot firstSpot = new ParkingSpot("s1", "A-1", SpotType.COMPACT);
        ParkingSpot secondSpot = new ParkingSpot("s2", "A-2", SpotType.LARGE);
        ParkingLot lot = singleFloorLot(firstSpot, secondSpot);
        when(allocationStrategy.allocate(anyList(), same(car)))
                .thenReturn(Optional.of(firstSpot))
                .thenReturn(Optional.of(secondSpot));
        when(spotRepository.findByIdForUpdate("s1")).thenReturn(Optional.of(firstSpot));
        when(spotRepository.findByIdForUpdate("s2")).thenReturn(Optional.of(secondSpot));

        ParkingTicket firstTicket = service.admit(car, lot);
        ParkingTicket secondTicket = service.admit(car, lot);

        assertNotEquals(firstTicket.getId(), secondTicket.getId());
        assertEquals(SpotStatus.OCCUPIED, firstSpot.getStatus());
        assertEquals(SpotStatus.OCCUPIED, secondSpot.getStatus());
    }

    @Test
    void rejectsNullArguments() {
        ParkingLot lot = singleFloorLot(new ParkingSpot("s1", "A-1", SpotType.COMPACT));

        assertThrows(NullPointerException.class, () -> service.admit(null, lot));
        assertThrows(NullPointerException.class, () -> service.admit(car, null));
    }

    @Test
    void rejectsNullConstructorDependencies() {
        Clock clock = Clock.fixed(ENTRY_INSTANT, ZoneOffset.UTC);

        assertThrows(NullPointerException.class,
                () -> new ParkingEntryService(null, spotRepository, ticketRepository, clock));
        assertThrows(NullPointerException.class,
                () -> new ParkingEntryService(allocationStrategy, null, ticketRepository, clock));
        assertThrows(NullPointerException.class,
                () -> new ParkingEntryService(allocationStrategy, spotRepository, null, clock));
        assertThrows(NullPointerException.class,
                () -> new ParkingEntryService(allocationStrategy, spotRepository, ticketRepository, null));
    }

    private ParkingLot singleFloorLot(ParkingSpot... spots) {
        return new ParkingLot("lot-1", "Rapid Lot",
                List.of(new ParkingFloor("f1", 1, List.of(spots))));
    }
}
