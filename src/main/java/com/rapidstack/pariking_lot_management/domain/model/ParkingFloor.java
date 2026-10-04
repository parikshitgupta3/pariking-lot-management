package com.rapidstack.pariking_lot_management.domain.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * One floor of a parking lot, holding the floor's parking spots.
 *
 * <p>Immutable in structure: the spot list is fixed at construction and
 * exposed as an unmodifiable view. Spot numbers must be unique within the
 * floor, since a spot is addressed by its number.
 */
public final class ParkingFloor {

    private final String id;
    private final int floorNumber;
    private final List<ParkingSpot> spots;

    public ParkingFloor(String id, int floorNumber, List<ParkingSpot> spots) {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(spots, "spots must not be null");
        Set<String> seenSpotNumbers = new HashSet<>();
        for (ParkingSpot spot : spots) {
            Objects.requireNonNull(spot, "spots must not contain null");
            if (!seenSpotNumbers.add(spot.getSpotNumber())) {
                throw new IllegalArgumentException("duplicate spotNumber on floor: " + spot.getSpotNumber());
            }
        }
        this.id = id;
        this.floorNumber = floorNumber;
        this.spots = List.copyOf(spots);
    }

    public String getId() {
        return id;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public List<ParkingSpot> getSpots() {
        return spots;
    }

    /**
     * Identity is the id; two floors with the same id are the same floor
     * regardless of the spots they currently hold.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParkingFloor that)) {
            return false;
        }
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ParkingFloor{id='" + id + "', floorNumber=" + floorNumber + ", spots=" + spots.size() + "}";
    }
}
