package com.rapidstack.pariking_lot_management.api.dto;

import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.TicketStatus;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representation of a parking ticket. {@code exitTime} and {@code fee} are
 * {@code null} while the ticket is ACTIVE.
 */
public record TicketResponse(
        String id,
        String vehicleRegistrationNumber,
        VehicleType vehicleType,
        String spotNumber,
        SpotType spotType,
        Instant entryTime,
        Instant exitTime,
        BigDecimal fee,
        TicketStatus status) {

    public static TicketResponse from(ParkingTicket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getVehicle().getRegistrationNumber(),
                ticket.getVehicle().getVehicleType(),
                ticket.getSpot().getSpotNumber(),
                ticket.getSpot().getSpotType(),
                ticket.getEntryTime(),
                ticket.getExitTime(),
                ticket.getFee(),
                ticket.getStatus());
    }
}
