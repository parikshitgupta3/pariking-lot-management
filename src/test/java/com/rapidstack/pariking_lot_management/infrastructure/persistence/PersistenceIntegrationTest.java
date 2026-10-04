package com.rapidstack.pariking_lot_management.infrastructure.persistence;

import com.rapidstack.pariking_lot_management.application.port.ParkingTicketRepository;
import com.rapidstack.pariking_lot_management.domain.enums.GateType;
import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.TicketStatus;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import com.rapidstack.pariking_lot_management.domain.model.ParkingFloor;
import com.rapidstack.pariking_lot_management.domain.model.ParkingGate;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.adapter.SpotNotPersistedException;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingFloorEntity;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingGateEntity;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingLotEntity;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingSpotEntity;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.mapper.ParkingGateMapper;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.mapper.ParkingLotMapper;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.repository.ParkingFloorJpaRepository;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.repository.ParkingGateJpaRepository;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.repository.ParkingLotJpaRepository;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.repository.ParkingSpotJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private ParkingLotJpaRepository lots;

    @Autowired
    private ParkingFloorJpaRepository floors;

    @Autowired
    private ParkingSpotJpaRepository spots;

    @Autowired
    private ParkingGateJpaRepository gates;

    @Autowired
    private ParkingTicketRepository ticketPort;

    private static String id() {
        return UUID.randomUUID().toString();
    }

    @Test
    void lotAggregateRoundTripsThroughPostgres() {
        ParkingSpot occupiedCompact = new ParkingSpot(id(), "A-1", SpotType.COMPACT, SpotStatus.OCCUPIED);
        ParkingSpot freeBike = new ParkingSpot(id(), "A-2", SpotType.BIKE, SpotStatus.AVAILABLE);
        ParkingSpot large = new ParkingSpot(id(), "B-1", SpotType.LARGE, SpotStatus.AVAILABLE);
        ParkingLot lot = new ParkingLot(id(), "Rapid Lot", List.of(
                new ParkingFloor(id(), 1, List.of(occupiedCompact, freeBike)),
                new ParkingFloor(id(), 2, List.of(large))));

        lots.save(ParkingLotMapper.toEntity(lot));

        ParkingLot loaded = ParkingLotMapper.toDomain(lots.findById(lot.getId()).orElseThrow());
        assertEquals(lot.getId(), loaded.getId());
        assertEquals("Rapid Lot", loaded.getName());
        assertEquals(2, loaded.getFloors().size());
        assertEquals(1, loaded.getFloors().get(0).getFloorNumber());

        List<ParkingSpot> groundSpots = loaded.getFloors().get(0).getSpots();
        assertEquals(2, groundSpots.size());
        assertEquals("A-1", groundSpots.get(0).getSpotNumber());
        assertEquals(SpotType.COMPACT, groundSpots.get(0).getSpotType());
        assertEquals(SpotStatus.OCCUPIED, groundSpots.get(0).getStatus());
        assertEquals("A-2", groundSpots.get(1).getSpotNumber());
        assertEquals("B-1", loaded.getFloors().get(1).getSpots().get(0).getSpotNumber());
    }

    @Test
    void rejectsDuplicateSpotNumbersWithinAFloor() {
        ParkingLot lot = new ParkingLot(id(), "Dup Spot Lot", List.of(
                new ParkingFloor(id(), 1, List.of(new ParkingSpot(id(), "A-1", SpotType.COMPACT)))));
        lots.saveAndFlush(ParkingLotMapper.toEntity(lot));

        ParkingLotEntity managedLot = lots.findById(lot.getId()).orElseThrow();
        ParkingFloorEntity managedFloor = managedLot.getFloors().get(0);
        ParkingSpotEntity duplicate = new ParkingSpotEntity(id(), "A-1", SpotType.LARGE, SpotStatus.AVAILABLE);
        duplicate.setFloor(managedFloor);

        assertThrows(DataIntegrityViolationException.class, () -> spots.saveAndFlush(duplicate));
    }

    @Test
    void rejectsDuplicateFloorNumbersWithinALot() {
        ParkingLot lot = new ParkingLot(id(), "Dup Floor Lot", List.of(
                new ParkingFloor(id(), 1, List.of(new ParkingSpot(id(), "A-1", SpotType.COMPACT)))));
        lots.saveAndFlush(ParkingLotMapper.toEntity(lot));

        ParkingFloorEntity duplicate = new ParkingFloorEntity(id(), 1);
        duplicate.setLot(lots.findById(lot.getId()).orElseThrow());

        assertThrows(DataIntegrityViolationException.class, () -> floors.saveAndFlush(duplicate));
    }

    @Test
    void rejectsDuplicateGateNumbersWithinALot() {
        ParkingLot lot = new ParkingLot(id(), "Gate Lot", List.of(
                new ParkingFloor(id(), 1, List.of(new ParkingSpot(id(), "A-1", SpotType.COMPACT)))));
        ParkingLotEntity lotEntity = ParkingLotMapper.toEntity(lot);
        lots.saveAndFlush(lotEntity);
        ParkingLotEntity managedLot = lots.findById(lot.getId()).orElseThrow();

        gates.saveAndFlush(ParkingGateMapper.toEntity(new ParkingGate(id(), 1, GateType.ENTRY), managedLot));

        ParkingGateEntity duplicate = ParkingGateMapper.toEntity(new ParkingGate(id(), 1, GateType.EXIT), managedLot);

        assertThrows(DataIntegrityViolationException.class, () -> gates.saveAndFlush(duplicate));
    }

    @Test
    void ticketPortRoundTripsActiveAndCompletedTickets() {
        ParkingSpot spot = new ParkingSpot(id(), "A-1", SpotType.COMPACT);
        ParkingLot lot = new ParkingLot(id(), "Ticket Lot", List.of(
                new ParkingFloor(id(), 1, List.of(spot))));
        lots.save(ParkingLotMapper.toEntity(lot));

        spot.markOccupied();
        Vehicle car = new Vehicle("KA01AB" + id().substring(0, 4).toUpperCase(), VehicleType.CAR);
        Instant entry = Instant.parse("2026-10-04T10:00:00Z");
        ParkingTicket ticket = new ParkingTicket(id(), car, spot, entry);
        ticketPort.save(ticket);

        ParkingTicket activeTicket = ticketPort.findById(ticket.getId()).orElseThrow();
        assertEquals(TicketStatus.ACTIVE, activeTicket.getStatus());
        assertEquals(car, activeTicket.getVehicle());
        assertEquals(spot.getId(), activeTicket.getSpot().getId());
        assertEquals(entry, activeTicket.getEntryTime());
        assertTrue(activeTicket.getExitTime() == null && activeTicket.getFee() == null);

        Instant exit = Instant.parse("2026-10-04T11:30:00Z");
        activeTicket.close(exit, new BigDecimal("15.00"));
        activeTicket.getSpot().release();
        ticketPort.save(activeTicket);

        ParkingTicket completedTicket = ticketPort.findById(ticket.getId()).orElseThrow();
        assertEquals(TicketStatus.COMPLETED, completedTicket.getStatus());
        assertEquals(exit, completedTicket.getExitTime());
        assertEquals(0, new BigDecimal("15.00").compareTo(completedTicket.getFee()));

        ParkingSpotEntity persistedSpot = spots.findById(spot.getId()).orElseThrow();
        assertEquals(SpotStatus.AVAILABLE, persistedSpot.getStatus());
    }

    @Test
    void ticketPortRejectsTicketsForUnpersistedSpots() {
        ParkingSpot floatingSpot = new ParkingSpot(id(), "Z-9", SpotType.LARGE);
        ParkingTicket ticket = new ParkingTicket(id(),
                new Vehicle("KA01XY" + id().substring(0, 4).toUpperCase(), VehicleType.TRUCK),
                floatingSpot, Instant.parse("2026-10-04T09:00:00Z"));

        assertThrows(SpotNotPersistedException.class, () -> ticketPort.save(ticket));
    }
}
