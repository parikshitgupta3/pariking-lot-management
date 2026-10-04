package com.rapidstack.pariking_lot_management.infrastructure.persistence.mapper;

import com.rapidstack.pariking_lot_management.domain.model.ParkingFloor;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingLotEntity;

import java.util.List;

/**
 * Maps the {@link ParkingLot} aggregate (with its floors and spots) to and
 * from {@link ParkingLotEntity}.
 *
 * <p>Round trips are order-normalised: entities are loaded ordered by
 * floor number and spot number, so a reloaded domain lot lists floors and
 * spots in that order regardless of the order they were constructed in.
 */
public final class ParkingLotMapper {

    private ParkingLotMapper() {
    }

    public static ParkingLotEntity toEntity(ParkingLot lot) {
        ParkingLotEntity entity = new ParkingLotEntity(lot.getId(), lot.getName());
        for (ParkingFloor floor : lot.getFloors()) {
            entity.addFloor(ParkingFloorMapper.toEntity(floor));
        }
        return entity;
    }

    public static ParkingLot toDomain(ParkingLotEntity entity) {
        List<ParkingFloor> floors = entity.getFloors().stream()
                .map(ParkingFloorMapper::toDomain)
                .toList();
        return new ParkingLot(entity.getId(), entity.getName(), floors);
    }
}
