package com.rapidstack.pariking_lot_management.api.dto;

import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.model.ParkingFloor;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request body for creating a parking lot with its floors and spots. Ids
 * are generated server-side; the client supplies only meaningful data
 * (names, floor numbers, spot numbers, spot types). New spots start
 * AVAILABLE.
 */
public record CreateParkingLotRequest(
        @NotBlank(message = "name must not be blank") String name,
        @NotEmpty(message = "floors must contain at least one floor") @Valid List<CreateFloorRequest> floors) {

    public record CreateFloorRequest(
            @NotNull(message = "floorNumber must not be null") Integer floorNumber,
            @NotEmpty(message = "spots must contain at least one spot") @Valid List<CreateSpotRequest> spots) {
    }

    public record CreateSpotRequest(
            @NotBlank(message = "spotNumber must not be blank") String spotNumber,
            @NotNull(message = "spotType must not be null") SpotType spotType) {
    }

    public ParkingLot toDomain() {
        List<ParkingFloor> floors = this.floors.stream()
                .map(floorRequest -> {
                    List<ParkingSpot> spots = floorRequest.spots().stream()
                            .map(spotRequest -> new ParkingSpot(
                                    UUID.randomUUID().toString(),
                                    spotRequest.spotNumber(),
                                    spotRequest.spotType()))
                            .toList();
                    return new ParkingFloor(UUID.randomUUID().toString(), floorRequest.floorNumber(), spots);
                })
                .toList();
        return new ParkingLot(UUID.randomUUID().toString(), name, floors);
    }
}
