package com.rapidstack.pariking_lot_management.domain.model;

import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParkingSpotTest {

    private ParkingSpot availableCompactSpot() {
        return new ParkingSpot("spot-1", "A-1", SpotType.COMPACT);
    }

    @Test
    void newSpotStartsAvailable() {
        assertEquals(SpotStatus.AVAILABLE, availableCompactSpot().getStatus());
    }

    @Test
    void markingAvailableSpotOccupiesIt() {
        ParkingSpot spot = availableCompactSpot();

        spot.markOccupied();

        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
    }

    @Test
    void occupiedSpotCannotBeOccupiedAgain() {
        ParkingSpot spot = availableCompactSpot();
        spot.markOccupied();

        assertThrows(IllegalStateException.class, spot::markOccupied);
    }

    @Test
    void outOfServiceSpotCannotBeOccupied() {
        ParkingSpot spot = new ParkingSpot("spot-1", "A-1", SpotType.COMPACT, SpotStatus.OUT_OF_SERVICE);

        assertThrows(IllegalStateException.class, spot::markOccupied);
    }

    @Test
    void releasingOccupiedSpotMakesItAvailableAgain() {
        ParkingSpot spot = availableCompactSpot();
        spot.markOccupied();

        spot.release();

        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }

    @Test
    void releasingOccupiedSpotAllowsItToBeReoccupied() {
        ParkingSpot spot = availableCompactSpot();
        spot.markOccupied();
        spot.release();

        spot.markOccupied();

        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
    }

    @Test
    void availableSpotCannotBeReleased() {
        ParkingSpot spot = availableCompactSpot();

        assertThrows(IllegalStateException.class, spot::release);
    }

    @Test
    void outOfServiceSpotCannotBeReleased() {
        ParkingSpot spot = new ParkingSpot("spot-1", "A-1", SpotType.COMPACT, SpotStatus.OUT_OF_SERVICE);

        assertThrows(IllegalStateException.class, spot::release);
    }
}
