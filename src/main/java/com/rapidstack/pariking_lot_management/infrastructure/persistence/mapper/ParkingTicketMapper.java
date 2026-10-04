package com.rapidstack.pariking_lot_management.infrastructure.persistence.mapper;

import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingSpotEntity;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingTicketEntity;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.VehicleEntity;

/**
 * Maps {@link ParkingTicket} domain objects to and from {@link ParkingTicketEntity}.
 *
 * <p>The entity's vehicle and spot references must be managed entities —
 * the adapter is responsible for resolving them before mapping; this mapper
 * only assembles.
 */
public final class ParkingTicketMapper {

    private ParkingTicketMapper() {
    }

    public static ParkingTicketEntity toEntity(ParkingTicket ticket, VehicleEntity vehicle, ParkingSpotEntity spot) {
        return new ParkingTicketEntity(ticket.getId(), vehicle, spot,
                ticket.getEntryTime(), ticket.getExitTime(), ticket.getFee(), ticket.getStatus());
    }

    public static ParkingTicket toDomain(ParkingTicketEntity entity) {
        return new ParkingTicket(entity.getId(),
                VehicleMapper.toDomain(entity.getVehicle()),
                ParkingSpotMapper.toDomain(entity.getSpot()),
                entity.getEntryTime(), entity.getExitTime(), entity.getFee(), entity.getStatus());
    }
}
