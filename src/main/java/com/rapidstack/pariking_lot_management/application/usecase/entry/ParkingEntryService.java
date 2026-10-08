package com.rapidstack.pariking_lot_management.application.usecase.entry;

import com.rapidstack.pariking_lot_management.application.port.ParkingSpotRepository;
import com.rapidstack.pariking_lot_management.application.port.ParkingTicketRepository;
import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.exception.NoAvailableSpotException;
import com.rapidstack.pariking_lot_management.domain.model.ParkingFloor;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;
import com.rapidstack.pariking_lot_management.domain.strategy.allocation.SpotAllocationStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ParkingEntryService {

    private final SpotAllocationStrategy allocationStrategy;
    private final ParkingSpotRepository spotRepository;
    private final ParkingTicketRepository ticketRepository;
    private final Clock clock;

    public ParkingEntryService(SpotAllocationStrategy allocationStrategy,
                               ParkingSpotRepository spotRepository,
                               ParkingTicketRepository ticketRepository,
                               Clock clock) {
        this.allocationStrategy = Objects.requireNonNull(allocationStrategy, "allocationStrategy must not be null");
        this.spotRepository = Objects.requireNonNull(spotRepository, "spotRepository must not be null");
        this.ticketRepository = Objects.requireNonNull(ticketRepository, "ticketRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * Admits a vehicle to the lot: allocates a spot, occupies it, opens a
     * ticket for the stay, and persists it — atomically.
     *
     * @throws NoAvailableSpotException if no available spot fits the vehicle
     */
    @Transactional
    public ParkingTicket admit(Vehicle vehicle, ParkingLot parkingLot) {
        Objects.requireNonNull(vehicle, "vehicle must not be null");
        Objects.requireNonNull(parkingLot, "parkingLot must not be null");

        List<ParkingSpot> candidates = new ArrayList<>(parkingLot.getFloors().stream()
                .map(ParkingFloor::getSpots)
                .flatMap(List::stream)
                .toList());

        // Each failed confirmation removes exactly one candidate, so the loop
        // runs at most candidates.size() rounds — it cannot spin forever.
        while (!candidates.isEmpty()) {
            // Defensive copy per round: the strategy sees a stable snapshot,
            // and excluded candidates never leak into earlier rounds' views.
            ParkingSpot chosen = allocationStrategy.allocate(List.copyOf(candidates), vehicle)
                    .orElseThrow(() -> noAvailableSpot(vehicle, parkingLot));

            ParkingSpot locked = spotRepository.findByIdForUpdate(chosen.getId())
                    .orElse(null);
            if (locked == null || locked.getStatus() != SpotStatus.AVAILABLE) {
                // Taken by a concurrent entry (or gone) — try the remaining candidates.
                candidates.remove(chosen);
                continue;
            }

            locked.markOccupied();
            ParkingTicket ticket = new ParkingTicket(UUID.randomUUID().toString(), vehicle, locked, clock.instant());
            ticketRepository.save(ticket);
            return ticket;
        }
        throw noAvailableSpot(vehicle, parkingLot);
    }

    private static NoAvailableSpotException noAvailableSpot(Vehicle vehicle, ParkingLot parkingLot) {
        return new NoAvailableSpotException(
                "No available spot for vehicle " + vehicle.getRegistrationNumber()
                        + " (" + vehicle.getVehicleType() + ") at lot '" + parkingLot.getName() + "'");
    }
}
