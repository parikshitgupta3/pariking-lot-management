package com.rapidstack.pariking_lot_management.infrastructure.persistence.mapper;

import com.rapidstack.pariking_lot_management.domain.model.ParkingGate;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingGateEntity;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingLotEntity;

/**
 * Maps {@link ParkingGate} domain objects to and from {@link ParkingGateEntity}.
 *
 * <p>{@link #toEntity} takes the owning lot because the schema requires the
 * gate→lot foreign key; the domain model has no such reference, so
 * {@link #toDomain} simply drops it.
 */
public final class ParkingGateMapper {

    private ParkingGateMapper() {
    }

    public static ParkingGateEntity toEntity(ParkingGate gate, ParkingLotEntity lot) {
        ParkingGateEntity entity = new ParkingGateEntity(gate.getId(), gate.getGateNumber(), gate.getGateType());
        entity.setLot(lot);
        return entity;
    }

    public static ParkingGate toDomain(ParkingGateEntity entity) {
        return new ParkingGate(entity.getId(), entity.getGateNumber(), entity.getGateType());
    }
}
