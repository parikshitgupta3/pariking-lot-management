package com.rapidstack.pariking_lot_management.api.dto;

import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;

import java.util.List;

/**
 * Full representation of a parking lot — floors with their spots and
 * current statuses — for detail responses.
 */
public record ParkingLotDetailsResponse(String id, String name, List<FloorResponse> floors) {

    public record FloorResponse(int floorNumber, List<SpotResponse> spots) {
    }

    public record SpotResponse(String spotNumber, SpotType spotType, SpotStatus status) {
    }

    public static ParkingLotDetailsResponse from(ParkingLot lot) {
        return new ParkingLotDetailsResponse(
                lot.getId(),
                lot.getName(),
                lot.getFloors().stream()
                        .map(floor -> new FloorResponse(
                                floor.getFloorNumber(),
                                floor.getSpots().stream()
                                        .map(spot -> new SpotResponse(
                                                spot.getSpotNumber(), spot.getSpotType(), spot.getStatus()))
                                        .toList()))
                        .toList());
    }
}
