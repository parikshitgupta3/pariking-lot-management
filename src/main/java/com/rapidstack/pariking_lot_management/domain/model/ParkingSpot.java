package com.rapidstack.pariking_lot_management.domain.model;

import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;

import java.util.Objects;

/**
 * A single parking spot on a floor.
 *
 * <p>The spot's identity (id, spotNumber) and size class are fixed at
 * creation. Its status follows a lifecycle: AVAILABLE spots are occupied via
 * {@link #markOccupied()} and freed via {@link #release()}. Both transitions
 * validate the current state, so an unavailable spot can never be allocated
 * and an unoccupied spot can never be released. Administrative state changes
 * (e.g. taking a spot out of service) go through
 * {@link #setStatus(SpotStatus)}.
 */
public final class ParkingSpot {

    private final String id;
    private final String spotNumber;
    private final SpotType spotType;
    private SpotStatus status;

    public ParkingSpot(String id, String spotNumber, SpotType spotType, SpotStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(spotNumber, "spotNumber must not be null");
        if (spotNumber.isBlank()) {
            throw new IllegalArgumentException("spotNumber must not be blank");
        }
        this.id = id;
        this.spotNumber = spotNumber;
        this.spotType = Objects.requireNonNull(spotType, "spotType must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    /**
     * Creates a spot that is initially available for use.
     */
    public ParkingSpot(String id, String spotNumber, SpotType spotType) {
        this(id, spotNumber, spotType, SpotStatus.AVAILABLE);
    }

    /**
     * Occupies this spot.
     *
     * @throws IllegalStateException if the spot is not currently available
     *                               (already occupied or out of service)
     */
    public void markOccupied() {
        if (status != SpotStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "spot " + spotNumber + " cannot be occupied (current status: " + status + ")");
        }
        this.status = SpotStatus.OCCUPIED;
    }

    /**
     * Releases this spot back to the available pool.
     *
     * @throws IllegalStateException if the spot is not currently occupied
     */
    public void release() {
        if (status != SpotStatus.OCCUPIED) {
            throw new IllegalStateException(
                    "spot " + spotNumber + " cannot be released (current status: " + status + ")");
        }
        this.status = SpotStatus.AVAILABLE;
    }

    /**
     * Administrative status control, e.g. taking a spot out of service for
     * maintenance or returning it. Occupancy lifecycle transitions should use
     * {@link #markOccupied()} and {@link #release()}.
     */
    public void setStatus(SpotStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public String getId() {
        return id;
    }

    public String getSpotNumber() {
        return spotNumber;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public SpotStatus getStatus() {
        return status;
    }

    /**
     * Identity is the id; two spots with the same id are the same spot
     * regardless of their current status.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParkingSpot that)) {
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
        return "ParkingSpot{id='" + id + "', spotNumber='" + spotNumber + "', spotType=" + spotType
                + ", status=" + status + "}";
    }
}
