package com.rapidstack.pariking_lot_management.application.usecase.parkinglot;

import com.rapidstack.pariking_lot_management.application.exception.ParkingLotNotFoundException;
import com.rapidstack.pariking_lot_management.application.port.ParkingLotRepository;
import com.rapidstack.pariking_lot_management.application.port.ParkingTicketRepository;
import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates parking-lot management: creating lots (with their floors and
 * spots), looking them up, and answering availability queries. The service
 * owns no business rules — structural invariants (unique floor/spot numbers)
 * live in the domain model and the database; this class only delegates to
 * repository ports and assembles query views.
 */
@Service
public class ParkingLotService {

    private final ParkingLotRepository parkingLots;
    private final ParkingTicketRepository tickets;

    public ParkingLotService(ParkingLotRepository parkingLots, ParkingTicketRepository tickets) {
        this.parkingLots = Objects.requireNonNull(parkingLots, "parkingLots must not be null");
        this.tickets = Objects.requireNonNull(tickets, "tickets must not be null");
    }

    public ParkingLot create(ParkingLot lot) {
        return parkingLots.save(lot);
    }

    public ParkingLot getById(String id) {
        return parkingLots.findById(id)
                .orElseThrow(() -> new ParkingLotNotFoundException("No parking lot found for id '" + id + "'"));
    }

    public List<ParkingLot> getAll() {
        return parkingLots.findAll();
    }

    /**
     * A lot paired with its currently-active tickets, keyed by the id of the
     * spot each ticket occupies. Occupied spots without an active ticket
     * (e.g. taken out of service while occupied in a future scenario) simply
     * have no map entry.
     */
    public record LotDetail(ParkingLot lot, Map<String, ParkingTicket> activeTicketsBySpotId) {
    }

    /**
     * @return the lot together with the active ticket occupying each spot,
     *         so clients can show vehicle details and run the exit workflow
     */
    public LotDetail getDetail(String id) {
        ParkingLot lot = getById(id);
        Map<String, ParkingTicket> activeTicketsBySpotId = tickets.findActiveByLotId(id).stream()
                .collect(Collectors.toMap(ticket -> ticket.getSpot().getId(), Function.identity()));
        return new LotDetail(lot, activeTicketsBySpotId);
    }

    /**
     * @return every currently available spot in the lot, paired with its
     *         floor number, in floor/spot order
     */
    public List<AvailableSpotView> getAvailability(String lotId) {
        ParkingLot lot = getById(lotId);
        return lot.getFloors().stream()
                .flatMap(floor -> floor.getSpots().stream()
                        .filter(spot -> spot.getStatus() == SpotStatus.AVAILABLE)
                        .map(spot -> new AvailableSpotView(spot.getId(), floor.getFloorNumber(),
                                spot.getSpotNumber(), spot.getSpotType())))
                .toList();
    }
}
