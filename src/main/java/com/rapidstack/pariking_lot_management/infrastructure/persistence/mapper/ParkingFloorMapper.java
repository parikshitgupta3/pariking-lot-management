package com.rapidstack.pariking_lot_management.infrastructure.persistence.mapper;

import com.rapidstack.pariking_lot_management.domain.model.ParkingFloor;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingFloorEntity;

import java.util.List;

/**
 * Maps {@link ParkingFloor} domain objects to and from {@link ParkingFloorEntity}.
 */
public final class ParkingFloorMapper {

    private ParkingFloorMapper() {
    }

    public static ParkingFloorEntity toEntity(ParkingFloor floor) {
        ParkingFloorEntity entity = new ParkingFloorEntity(floor.getId(), floor.getFloorNumber());
        floor.getSpots().stream()
                .map(ParkingSpotMapper::toEntity)
                .forEach(entity::addSpot);
        return entity;
    }

    public static ParkingFloor toDomain(ParkingFloorEntity entity) {
        List<ParkingSpot> spots = entity.getSpots().stream()
                .map(ParkingSpotMapper::toDomain)
                .toList();
        return new ParkingFloor(entity.getId(), entity.getFloorNumber(), spots);
    }
}
