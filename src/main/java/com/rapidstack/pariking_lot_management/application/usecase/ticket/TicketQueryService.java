package com.rapidstack.pariking_lot_management.application.usecase.ticket;

import com.rapidstack.pariking_lot_management.application.port.ParkingLotRepository;
import com.rapidstack.pariking_lot_management.application.port.ParkingTicketRepository;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read-side ticket queries. The domain {@link ParkingTicket} knows its spot
 * but not the spot's floor or lot, so this service joins the location in from
 * the lot aggregates, keyed by spot id.
 */
@Service
public class TicketQueryService {

    private final ParkingTicketRepository tickets;
    private final ParkingLotRepository parkingLots;

    public TicketQueryService(ParkingTicketRepository tickets, ParkingLotRepository parkingLots) {
        this.tickets = Objects.requireNonNull(tickets, "tickets must not be null");
        this.parkingLots = Objects.requireNonNull(parkingLots, "parkingLots must not be null");
    }

    private record SpotLocation(int floorNumber, String lotId, String lotName) {
    }

    /**
     * @return every active ticket across all lots, oldest entry first
     */
    public List<ActiveTicketView> getActiveTickets() {
        Map<String, SpotLocation> locationBySpotId = parkingLots.findAll().stream()
                .flatMap(lot -> lot.getFloors().stream()
                        .flatMap(floor -> floor.getSpots().stream()
                                .map(spot -> Map.entry(spot.getId(),
                                        new SpotLocation(floor.getFloorNumber(), lot.getId(), lot.getName())))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return tickets.findAllActive().stream()
                .map(ticket -> {
                    SpotLocation location = locationBySpotId.get(ticket.getSpot().getId());
                    Objects.requireNonNull(location,
                            "ticket " + ticket.getId() + " references spot " + ticket.getSpot().getId()
                                    + " that belongs to no known lot");
                    return new ActiveTicketView(
                            ticket.getId(),
                            ticket.getVehicle().getRegistrationNumber(),
                            ticket.getVehicle().getVehicleType(),
                            ticket.getSpot().getSpotNumber(),
                            ticket.getSpot().getSpotType(),
                            location.floorNumber(),
                            location.lotId(),
                            location.lotName(),
                            ticket.getEntryTime());
                })
                .sorted(Comparator.comparing(ActiveTicketView::entryTime))
                .toList();
    }
}
