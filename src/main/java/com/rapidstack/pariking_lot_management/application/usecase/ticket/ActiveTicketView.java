package com.rapidstack.pariking_lot_management.application.usecase.ticket;

import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;

import java.time.Instant;

/**
 * A read model of one active ticket, enriched with the location of its spot
 * (floor number and owning lot) — the domain ticket's spot does not carry
 * floor or lot references, so the query service joins them in.
 */
public record ActiveTicketView(
        String id,
        String vehicleRegistrationNumber,
        VehicleType vehicleType,
        String spotNumber,
        SpotType spotType,
        int floorNumber,
        String lotId,
        String lotName,
        Instant entryTime) {
}
