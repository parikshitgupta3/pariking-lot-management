package com.rapidstack.pariking_lot_management.api.dto;

import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Full representation of a parking lot — floors with their spots, current
 * statuses, and the active ticket occupying each spot — for detail responses.
 */
public record ParkingLotDetailsResponse(String id, String name, List<FloorResponse> floors) {

    public record FloorResponse(int floorNumber, List<SpotResponse> spots) {
    }

    public record SpotResponse(String spotNumber, SpotType spotType, SpotStatus status,
                               ActiveTicketResponse ticket) {
    }

    /**
     * The active ticket occupying a spot; {@code null} when the spot is not
     * occupied (or has no active ticket).
     */
    public record ActiveTicketResponse(String id, String vehicleRegistrationNumber,
                                       VehicleType vehicleType, Instant entryTime) {
    }

    public static ParkingLotDetailsResponse from(ParkingLot lot) {
        return from(lot, Map.of());
    }

    public static ParkingLotDetailsResponse from(ParkingLot lot, Map<String, ParkingTicket> activeTicketsBySpotId) {
        return new ParkingLotDetailsResponse(
                lot.getId(),
                lot.getName(),
                lot.getFloors().stream()
                        .map(floor -> new FloorResponse(
                                floor.getFloorNumber(),
                                floor.getSpots().stream()
                                        .map(spot -> new SpotResponse(
                                                spot.getSpotNumber(), spot.getSpotType(), spot.getStatus(),
                                                ticketResponse(activeTicketsBySpotId.get(spot.getId()))))
                                        .toList()))
                        .toList());
    }

    private static ActiveTicketResponse ticketResponse(ParkingTicket ticket) {
        if (ticket == null) {
            return null;
        }
        return new ActiveTicketResponse(
                ticket.getId(),
                ticket.getVehicle().getRegistrationNumber(),
                ticket.getVehicle().getVehicleType(),
                ticket.getEntryTime());
    }
}
