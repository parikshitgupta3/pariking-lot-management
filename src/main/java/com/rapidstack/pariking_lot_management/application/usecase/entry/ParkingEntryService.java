package com.rapidstack.pariking_lot_management.application.usecase.entry;

import com.rapidstack.pariking_lot_management.application.port.ParkingTicketRepository;
import com.rapidstack.pariking_lot_management.domain.exception.NoAvailableSpotException;
import com.rapidstack.pariking_lot_management.domain.model.ParkingFloor;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;
import com.rapidstack.pariking_lot_management.domain.strategy.allocation.SpotAllocationStrategy;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Orchestrates the vehicle entry workflow: gathers the lot's spots as
 * allocation candidates, delegates spot selection to the injected
 * {@link SpotAllocationStrategy}, marks the chosen spot occupied (a
 * domain-guarded transition), issues an ACTIVE ticket for the stay, and
 * records it through the ticket repository port.
 *
 * <p>This service owns no business rules — spot compatibility and selection
 * live in the strategy, occupancy transitions and ticket invariants in the
 * domain model. Time comes from the injected {@link Clock} so behaviour is
 * deterministic under test.
 */
@Service
public class ParkingEntryService {

    private final SpotAllocationStrategy allocationStrategy;
    private final Clock clock;
    private final ParkingTicketRepository ticketRepository;

    public ParkingEntryService(SpotAllocationStrategy allocationStrategy,
                               Clock clock,
                               ParkingTicketRepository ticketRepository) {
        this.allocationStrategy = Objects.requireNonNull(allocationStrategy, "allocationStrategy must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.ticketRepository = Objects.requireNonNull(ticketRepository, "ticketRepository must not be null");
    }

    /**
     * Admits a vehicle to the lot: allocates a spot, occupies it, opens a
     * ticket for the stay, and persists it.
     *
     * @throws NoAvailableSpotException if no available spot fits the vehicle
     */
    public ParkingTicket admit(Vehicle vehicle, ParkingLot parkingLot) {
        Objects.requireNonNull(vehicle, "vehicle must not be null");
        Objects.requireNonNull(parkingLot, "parkingLot must not be null");

        List<ParkingSpot> candidateSpots = parkingLot.getFloors().stream()
                .map(ParkingFloor::getSpots)
                .flatMap(List::stream)
                .toList();

        ParkingSpot spot = allocationStrategy.allocate(candidateSpots, vehicle)
                .orElseThrow(() -> new NoAvailableSpotException(
                        "No available spot for vehicle " + vehicle.getRegistrationNumber()
                                + " (" + vehicle.getVehicleType() + ") at lot '" + parkingLot.getName() + "'"));

        spot.markOccupied();
        ParkingTicket ticket = new ParkingTicket(UUID.randomUUID().toString(), vehicle, spot, clock.instant());
        ticketRepository.save(ticket);
        return ticket;
    }
}
