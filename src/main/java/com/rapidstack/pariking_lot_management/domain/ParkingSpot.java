package com.rapidstack.pariking_lot_management.domain;

import java.util.Objects;

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

    public ParkingSpot(String id, String spotNumber, SpotType spotType) {
        this(id, spotNumber, spotType, SpotStatus.AVAILABLE);
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

    public void setStatus(SpotStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

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
