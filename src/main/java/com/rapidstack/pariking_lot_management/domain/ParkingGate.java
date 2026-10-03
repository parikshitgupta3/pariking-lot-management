package com.rapidstack.pariking_lot_management.domain;

import java.util.Objects;

public final class ParkingGate {

    private final String id;
    private final int gateNumber;
    private final GateType gateType;

    public ParkingGate(String id, int gateNumber, GateType gateType) {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        this.id = id;
        this.gateNumber = gateNumber;
        this.gateType = Objects.requireNonNull(gateType, "gateType must not be null");
    }

    public String getId() {
        return id;
    }

    public int getGateNumber() {
        return gateNumber;
    }

    public GateType getGateType() {
        return gateType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParkingGate that)) {
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
        return "ParkingGate{id='" + id + "', gateNumber=" + gateNumber + ", gateType=" + gateType + "}";
    }
}
