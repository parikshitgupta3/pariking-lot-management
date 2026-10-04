package com.rapidstack.pariking_lot_management.domain.strategy;

import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Allocates the first spot in candidate order that is available and fits
 * the vehicle. Candidate order is the caller's allocation-preference order
 * (typically floor order, then spot order).
 */
public final class FirstAvailableSpotStrategy implements SpotAllocationStrategy {

    @Override
    public Optional<ParkingSpot> allocate(List<ParkingSpot> candidateSpots, Vehicle vehicle) {
        Objects.requireNonNull(candidateSpots, "candidateSpots must not be null");
        Objects.requireNonNull(vehicle, "vehicle must not be null");
        return candidateSpots.stream()
                .filter(spot -> spot.getStatus() == SpotStatus.AVAILABLE)
                .filter(spot -> VehicleSpotCompatibility.supports(spot.getSpotType(), vehicle.getVehicleType()))
                .findFirst();
    }
}
