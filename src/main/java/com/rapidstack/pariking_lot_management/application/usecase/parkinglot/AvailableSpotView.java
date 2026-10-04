package com.rapidstack.pariking_lot_management.application.usecase.parkinglot;

import com.rapidstack.pariking_lot_management.domain.enums.SpotType;

/**
 * A read model for the availability query: one available spot together with
 * the floor it sits on. The domain {@code ParkingSpot} has no floor
 * reference, so the pairing lives in this use-case view.
 */
public record AvailableSpotView(String spotId, int floorNumber, String spotNumber, SpotType spotType) {
}
