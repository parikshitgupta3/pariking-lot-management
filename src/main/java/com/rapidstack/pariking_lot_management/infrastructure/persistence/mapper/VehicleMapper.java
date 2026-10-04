package com.rapidstack.pariking_lot_management.infrastructure.persistence.mapper;

import com.rapidstack.pariking_lot_management.domain.model.Vehicle;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.VehicleEntity;

/**
 * Maps {@link Vehicle} domain objects to and from {@link VehicleEntity}.
 */
public final class VehicleMapper {

    private VehicleMapper() {
    }

    public static VehicleEntity toEntity(Vehicle vehicle) {
        return new VehicleEntity(vehicle.getRegistrationNumber(), vehicle.getVehicleType());
    }

    public static Vehicle toDomain(VehicleEntity entity) {
        return new Vehicle(entity.getRegistrationNumber(), entity.getVehicleType());
    }
}
