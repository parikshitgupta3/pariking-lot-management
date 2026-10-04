package com.rapidstack.pariking_lot_management.infrastructure.persistence.mapper;

import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingSpotEntity;

/**
 * Maps {@link ParkingSpot} domain objects to and from {@link ParkingSpotEntity}.
 *
 * <p>The floor association is set on the entity side via
 * {@code ParkingFloorEntity.addSpot} (or {@code setFloor}) — the domain
 * model has no spot→floor reference, so mapping back never touches it.
 */
public final class ParkingSpotMapper {

    private ParkingSpotMapper() {
    }

    public static ParkingSpotEntity toEntity(ParkingSpot spot) {
        return new ParkingSpotEntity(spot.getId(), spot.getSpotNumber(), spot.getSpotType(), spot.getStatus());
    }

    public static ParkingSpot toDomain(ParkingSpotEntity entity) {
        return new ParkingSpot(entity.getId(), entity.getSpotNumber(), entity.getSpotType(), entity.getStatus());
    }
}
