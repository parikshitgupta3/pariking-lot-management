package com.rapidstack.pariking_lot_management.domain.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The parking lot aggregate root: a named collection of floors.
 *
 * <p>Immutable in structure: the floor list is fixed at construction and
 * exposed as an unmodifiable view. Floor numbers must be unique within the
 * lot.
 */
public final class ParkingLot {

    private final String id;
    private final String name;
    private final List<ParkingFloor> floors;

    public ParkingLot(String id, String name, List<ParkingFloor> floors) {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(floors, "floors must not be null");
        Set<Integer> seenFloorNumbers = new HashSet<>();
        for (ParkingFloor floor : floors) {
            Objects.requireNonNull(floor, "floors must not contain null");
            if (!seenFloorNumbers.add(floor.getFloorNumber())) {
                throw new IllegalArgumentException("duplicate floorNumber in lot: " + floor.getFloorNumber());
            }
        }
        this.id = id;
        this.name = name;
        this.floors = List.copyOf(floors);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<ParkingFloor> getFloors() {
        return floors;
    }

    /**
     * Identity is the id; two lots with the same id are the same lot
     * regardless of their name or floors.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParkingLot that)) {
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
        return "ParkingLot{id='" + id + "', name='" + name + "', floors=" + floors.size() + "}";
    }
}
