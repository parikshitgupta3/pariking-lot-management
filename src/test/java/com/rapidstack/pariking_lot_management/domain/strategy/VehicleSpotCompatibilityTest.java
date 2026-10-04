package com.rapidstack.pariking_lot_management.domain.strategy;

import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleSpotCompatibilityTest {

    @Test
    void bikeFitsBikeCompactAndLargeSpots() {
        assertTrue(VehicleSpotCompatibility.supports(SpotType.BIKE, VehicleType.BIKE));
        assertTrue(VehicleSpotCompatibility.supports(SpotType.COMPACT, VehicleType.BIKE));
        assertTrue(VehicleSpotCompatibility.supports(SpotType.LARGE, VehicleType.BIKE));
    }

    @Test
    void carFitsCompactAndLargeSpotsButNotBikeSpots() {
        assertFalse(VehicleSpotCompatibility.supports(SpotType.BIKE, VehicleType.CAR));
        assertTrue(VehicleSpotCompatibility.supports(SpotType.COMPACT, VehicleType.CAR));
        assertTrue(VehicleSpotCompatibility.supports(SpotType.LARGE, VehicleType.CAR));
    }

    @Test
    void truckFitsOnlyLargeSpots() {
        assertFalse(VehicleSpotCompatibility.supports(SpotType.BIKE, VehicleType.TRUCK));
        assertFalse(VehicleSpotCompatibility.supports(SpotType.COMPACT, VehicleType.TRUCK));
        assertTrue(VehicleSpotCompatibility.supports(SpotType.LARGE, VehicleType.TRUCK));
    }

    @Test
    void supportedSpotTypesCoversEveryVehicleType() {
        for (VehicleType vehicleType : VehicleType.values()) {
            assertFalse(VehicleSpotCompatibility.supportedSpotTypes(vehicleType).isEmpty(),
                    "no spot types mapped for " + vehicleType);
        }
    }

    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class,
                () -> VehicleSpotCompatibility.supports(null, VehicleType.CAR));
        assertThrows(NullPointerException.class,
                () -> VehicleSpotCompatibility.supports(SpotType.LARGE, null));
        assertThrows(NullPointerException.class,
                () -> VehicleSpotCompatibility.supportedSpotTypes(null));
    }
}
