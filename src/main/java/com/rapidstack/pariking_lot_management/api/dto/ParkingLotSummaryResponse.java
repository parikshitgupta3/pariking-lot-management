package com.rapidstack.pariking_lot_management.api.dto;

import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;

/**
 * Compact representation of a parking lot for list responses.
 */
public record ParkingLotSummaryResponse(String id, String name, int floorCount, int totalSpots) {

    public static ParkingLotSummaryResponse from(ParkingLot lot) {
        return new ParkingLotSummaryResponse(
                lot.getId(),
                lot.getName(),
                lot.getFloors().size(),
                lot.getFloors().stream()
                        .mapToInt(floor -> floor.getSpots().size())
                        .sum());
    }
}
