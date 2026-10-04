package com.rapidstack.pariking_lot_management.domain.strategy;

import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstAvailableSpotStrategyTest {

    private final FirstAvailableSpotStrategy strategy = new FirstAvailableSpotStrategy();

    private static Vehicle vehicle(VehicleType vehicleType) {
        return new Vehicle("AB12CD3456", vehicleType);
    }

    @Test
    void allocatesFirstAvailableCompatibleSpotInCandidateOrder() {
        ParkingSpot bikeSpot = new ParkingSpot("s1", "A-1", SpotType.BIKE);                     // wrong size
        ParkingSpot occupiedCompact = new ParkingSpot("s2", "A-2", SpotType.COMPACT, SpotStatus.OCCUPIED);
        ParkingSpot firstFreeCompact = new ParkingSpot("s3", "A-3", SpotType.COMPACT);
        ParkingSpot laterFreeLarge = new ParkingSpot("s4", "A-4", SpotType.LARGE);

        Optional<ParkingSpot> result = strategy.allocate(
                List.of(bikeSpot, occupiedCompact, firstFreeCompact, laterFreeLarge), vehicle(VehicleType.CAR));

        assertTrue(result.isPresent());
        assertEquals("A-3", result.orElseThrow().getSpotNumber());
    }

    @Test
    void returnsEmptyWhenAllCompatibleSpotsAreUnavailable() {
        ParkingSpot occupiedCompact = new ParkingSpot("s1", "A-1", SpotType.COMPACT, SpotStatus.OCCUPIED);
        ParkingSpot servicedCompact = new ParkingSpot("s2", "A-2", SpotType.COMPACT, SpotStatus.OUT_OF_SERVICE);
        ParkingSpot freeBikeSpot = new ParkingSpot("s3", "A-3", SpotType.BIKE);                 // free but wrong size

        Optional<ParkingSpot> result = strategy.allocate(
                List.of(occupiedCompact, servicedCompact, freeBikeSpot), vehicle(VehicleType.CAR));

        assertTrue(result.isEmpty());
    }

    @Test
    void neverAllocatesOutOfServiceSpotsEvenWhenCompatible() {
        ParkingSpot outOfServiceLarge = new ParkingSpot("s1", "A-1", SpotType.LARGE, SpotStatus.OUT_OF_SERVICE);

        Optional<ParkingSpot> result = strategy.allocate(List.of(outOfServiceLarge), vehicle(VehicleType.TRUCK));

        assertTrue(result.isEmpty());
    }

    @Test
    void neverAllocatesOccupiedSpots() {
        ParkingSpot occupied = new ParkingSpot("s1", "A-1", SpotType.COMPACT, SpotStatus.OCCUPIED);

        Optional<ParkingSpot> result = strategy.allocate(List.of(occupied), vehicle(VehicleType.CAR));

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenNoCandidatesExist() {
        assertTrue(strategy.allocate(List.of(), vehicle(VehicleType.CAR)).isEmpty());
    }

    @Test
    void allocationSelectsWithoutOccupyingTheSpot() {
        ParkingSpot spot = new ParkingSpot("s1", "A-1", SpotType.COMPACT);

        strategy.allocate(List.of(spot), vehicle(VehicleType.CAR));

        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }

    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> strategy.allocate(null, vehicle(VehicleType.CAR)));
        assertThrows(NullPointerException.class, () -> strategy.allocate(List.of(), null));
    }
}
