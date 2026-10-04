package com.rapidstack.pariking_lot_management.domain.strategy;

import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;

import java.util.List;
import java.util.Optional;

/**
 * A policy for choosing a parking spot for a vehicle from a set of
 * candidate spots.
 *
 * <p>Implementations <em>select</em> a spot but do not mutate it: the caller
 * marks the chosen spot occupied (see
 * {@link ParkingSpot#markOccupied()}). That split keeps strategies pure and
 * makes the occupancy transition itself the single guarded point where an
 * unavailable spot is rejected.
 */
public interface SpotAllocationStrategy {

    /**
     * Selects a spot for the given vehicle.
     *
     * @param candidateSpots the spots to choose from, in allocation-preference
     *                       order (e.g. floor/position order)
     * @param vehicle        the vehicle needing a spot
     * @return the chosen spot, still in its current (available) state, or
     *         empty if no available spot fits the vehicle
     */
    Optional<ParkingSpot> allocate(List<ParkingSpot> candidateSpots, Vehicle vehicle);
}
