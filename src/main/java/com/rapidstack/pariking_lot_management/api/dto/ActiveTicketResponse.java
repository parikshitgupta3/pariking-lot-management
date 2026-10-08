package com.rapidstack.pariking_lot_management.api.dto;

import com.rapidstack.pariking_lot_management.application.usecase.ticket.ActiveTicketView;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;

import java.time.Instant;

/**
 * A row of the active-tickets list: ticket identity, vehicle, spot location
 * (spot, floor, lot), and entry time. Exit time and fee are absent by
 * definition while a ticket is active.
 */
public record ActiveTicketResponse(
        String id,
        String vehicleRegistrationNumber,
        VehicleType vehicleType,
        String spotNumber,
        SpotType spotType,
        int floorNumber,
        String lotId,
        String lotName,
        Instant entryTime) {

    public static ActiveTicketResponse from(ActiveTicketView view) {
        return new ActiveTicketResponse(
                view.id(),
                view.vehicleRegistrationNumber(),
                view.vehicleType(),
                view.spotNumber(),
                view.spotType(),
                view.floorNumber(),
                view.lotId(),
                view.lotName(),
                view.entryTime());
    }
}
