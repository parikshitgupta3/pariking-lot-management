package com.rapidstack.pariking_lot_management.infrastructure.persistence;

import com.rapidstack.pariking_lot_management.application.exception.TicketNotFoundException;
import com.rapidstack.pariking_lot_management.application.port.ParkingTicketRepository;
import com.rapidstack.pariking_lot_management.application.usecase.entry.ParkingEntryService;
import com.rapidstack.pariking_lot_management.application.usecase.exit.ParkingExitService;
import com.rapidstack.pariking_lot_management.application.usecase.parkinglot.ParkingLotService;
import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.TicketStatus;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import com.rapidstack.pariking_lot_management.domain.exception.NoAvailableSpotException;
import com.rapidstack.pariking_lot_management.domain.exception.TicketAlreadyCompletedException;
import com.rapidstack.pariking_lot_management.domain.model.ParkingFloor;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.repository.ParkingSpotJpaRepository;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.repository.ParkingTicketJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end lifecycle tests for the transactional entry/exit workflows
 * against real PostgreSQL (Flyway-migrated schema, Hibernate-validated
 * entities): success paths, failure paths, and the pessimistic-locking
 * guarantees under concurrency. All beans are real — no mocks.
 */
class VehicleLifecycleIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private ParkingLotService parkingLotService;

    @Autowired
    private ParkingEntryService parkingEntryService;

    @Autowired
    private ParkingExitService parkingExitService;

    @Autowired
    private ParkingTicketRepository ticketPort;

    @Autowired
    private ParkingSpotJpaRepository spots;

    @Autowired
    private ParkingTicketJpaRepository tickets;

    private static String id() {
        return UUID.randomUUID().toString();
    }

    private ParkingLot createLot(SpotType... spotTypes) {
        List<ParkingSpot> spots = new ArrayList<>();
        for (int i = 0; i < spotTypes.length; i++) {
            spots.add(new ParkingSpot(id(), "A-" + (i + 1), spotTypes[i]));
        }
        ParkingLot lot = new ParkingLot(id(), "Lot " + id().substring(0, 8),
                List.of(new ParkingFloor(id(), 1, spots)));
        return parkingLotService.create(lot);
    }

    @Test
    void fullLifecycleEntryThenExitIsAtomic() {
        ParkingLot lot = createLot(SpotType.COMPACT, SpotType.BIKE, SpotType.LARGE);
        Vehicle car = new Vehicle("KA01AB" + id().substring(0, 4).toUpperCase(), VehicleType.CAR);

        ParkingTicket ticket = parkingEntryService.admit(car, lot);

        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertEquals(car, ticket.getVehicle());
        assertNotNull(ticket.getEntryTime());
        assertEquals(SpotStatus.OCCUPIED, spots.findById(ticket.getSpot().getId()).orElseThrow().getStatus());

        ParkingTicket completed = parkingExitService.checkout(ticket.getId());

        assertEquals(TicketStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getExitTime());
        assertNotNull(completed.getFee());
        assertTrue(completed.getFee().signum() >= 0);
        assertEquals(SpotStatus.AVAILABLE, spots.findById(ticket.getSpot().getId()).orElseThrow().getStatus());

        ParkingTicket reloaded = ticketPort.findById(ticket.getId()).orElseThrow();
        assertEquals(TicketStatus.COMPLETED, reloaded.getStatus());
        assertEquals(0, completed.getFee().compareTo(reloaded.getFee()));
    }

    @Test
    void entryFailsWhenNoSpotFitsTheVehicle() {
        ParkingLot lot = createLot(SpotType.BIKE); // a car cannot use a bike spot
        ParkingSpot onlySpot = lot.getFloors().get(0).getSpots().get(0);

        NoAvailableSpotException exception = assertThrows(NoAvailableSpotException.class,
                () -> parkingEntryService.admit(new Vehicle("KA01CD" + id().substring(0, 4), VehicleType.CAR), lot));

        assertTrue(exception.getMessage().contains(lot.getName()));
        assertEquals(SpotStatus.AVAILABLE, spots.findById(onlySpot.getId()).orElseThrow().getStatus());
        assertTrue(tickets.findAll().stream()
                .noneMatch(ticket -> ticket.getSpot().getId().equals(onlySpot.getId())),
                "no ticket may be created for the rejected vehicle");
    }

    @Test
    void entryFailsWhenTheLotIsFull() {
        ParkingLot lot = createLot(SpotType.COMPACT);
        String spotId = lot.getFloors().get(0).getSpots().get(0).getId();

        parkingEntryService.admit(new Vehicle("KA01EF" + id().substring(0, 4), VehicleType.CAR), lot);

        assertThrows(NoAvailableSpotException.class,
                () -> parkingEntryService.admit(new Vehicle("KA01GH" + id().substring(0, 4), VehicleType.CAR), lot));
        assertEquals(1, tickets.findAll().stream()
                .filter(ticket -> ticket.getSpot().getId().equals(spotId))
                .count(), "only the first vehicle's ticket exists for the single spot");
    }

    @Test
    void exitRejectsUnknownTickets() {
        String unknownId = id();

        TicketNotFoundException exception = assertThrows(TicketNotFoundException.class,
                () -> parkingExitService.checkout(unknownId));

        assertTrue(exception.getMessage().contains(unknownId));
    }

    @Test
    void exitRejectsAlreadyCompletedTickets() {
        ParkingLot lot = createLot(SpotType.COMPACT);
        ParkingTicket ticket = parkingEntryService.admit(
                new Vehicle("KA01IJ" + id().substring(0, 4), VehicleType.CAR), lot);
        parkingExitService.checkout(ticket.getId());

        assertThrows(TicketAlreadyCompletedException.class, () -> parkingExitService.checkout(ticket.getId()));
    }

    @Test
    void concurrentEntriesNeverAllocateTheSameSpot() throws Exception {
        ParkingLot lot = createLot(SpotType.COMPACT, SpotType.COMPACT, SpotType.COMPACT, SpotType.COMPACT);
        List<String> spotIds = lot.getFloors().get(0).getSpots().stream().map(ParkingSpot::getId).toList();
        int threads = 8; // 4 spots, 8 contenders

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        try {
            List<Future<ParkingTicket>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                String registration = String.format("KA%04d%s", i, id().substring(0, 4).toUpperCase());
                Vehicle vehicle = new Vehicle(registration, VehicleType.CAR);
                futures.add(pool.submit(() -> {
                    startGate.await();
                    return parkingEntryService.admit(vehicle, lot);
                }));
            }
            startGate.countDown();

            List<ParkingTicket> successful = new ArrayList<>();
            int noSpotFailures = 0;
            for (Future<ParkingTicket> future : futures) {
                try {
                    successful.add(future.get(30, TimeUnit.SECONDS));
                } catch (ExecutionException e) {
                    assertTrue(e.getCause() instanceof NoAvailableSpotException,
                            "unexpected failure: " + e.getCause());
                    noSpotFailures++;
                }
            }

            assertEquals(4, successful.size(), "exactly the four spots should be taken");
            assertEquals(4, noSpotFailures, "the other contenders should be rejected");

            Set<String> occupiedSpotIds = new HashSet<>();
            for (ParkingTicket ticket : successful) {
                occupiedSpotIds.add(ticket.getSpot().getId());
            }
            assertEquals(4, occupiedSpotIds.size(), "each ticket must sit on a distinct spot");

            for (String spotId : spotIds) {
                assertEquals(SpotStatus.OCCUPIED, spots.findById(spotId).orElseThrow().getStatus());
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void concurrentCheckoutOfTheSameTicketSucceedsExactlyOnce() throws Exception {
        ParkingLot lot = createLot(SpotType.COMPACT);
        ParkingTicket ticket = parkingEntryService.admit(
                new Vehicle("KA01KL" + id().substring(0, 4), VehicleType.CAR), lot);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);
        try {
            List<Future<ParkingTicket>> futures = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                futures.add(pool.submit(() -> {
                    startGate.await();
                    return parkingExitService.checkout(ticket.getId());
                }));
            }
            startGate.countDown();

            int successes = 0;
            int rejections = 0;
            for (Future<ParkingTicket> future : futures) {
                try {
                    ParkingTicket completed = future.get(30, TimeUnit.SECONDS);
                    assertEquals(TicketStatus.COMPLETED, completed.getStatus());
                    successes++;
                } catch (ExecutionException e) {
                    assertTrue(e.getCause() instanceof TicketAlreadyCompletedException,
                            "unexpected failure: " + e.getCause());
                    rejections++;
                }
            }

            assertEquals(1, successes);
            assertEquals(1, rejections);
            assertEquals(SpotStatus.AVAILABLE, spots.findById(ticket.getSpot().getId()).orElseThrow().getStatus());
        } finally {
            pool.shutdownNow();
        }
    }
}
