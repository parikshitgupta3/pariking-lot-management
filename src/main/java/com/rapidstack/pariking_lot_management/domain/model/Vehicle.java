package com.rapidstack.pariking_lot_management.domain.model;

import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;

import java.util.Locale;
import java.util.Objects;

/**
 * A vehicle using the parking lot, identified by its registration number.
 *
 * <p>Instances are immutable and valid from creation: the constructor rejects
 * missing or blank registration numbers and a missing vehicle type, so a
 * {@code Vehicle} can never exist in an invalid state. The registration
 * number is stored in a canonical form (trimmed, upper-case) so that the same
 * plate always compares equal regardless of how it was entered.
 */
public final class Vehicle {

    private final String registrationNumber;
    private final VehicleType vehicleType;

    public Vehicle(String registrationNumber, VehicleType vehicleType) {
        Objects.requireNonNull(registrationNumber, "registrationNumber must not be null");
        if (registrationNumber.isBlank()) {
            throw new IllegalArgumentException("registrationNumber must not be blank");
        }
        this.registrationNumber = registrationNumber.trim().toUpperCase(Locale.ROOT);
        this.vehicleType = Objects.requireNonNull(vehicleType, "vehicleType must not be null");
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    /**
     * Equality is based on the registration number alone, since one plate
     * identifies exactly one vehicle.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vehicle vehicle)) {
            return false;
        }
        return registrationNumber.equals(vehicle.registrationNumber);
    }

    @Override
    public int hashCode() {
        return registrationNumber.hashCode();
    }

    @Override
    public String toString() {
        return "Vehicle{registrationNumber='" + registrationNumber + "', vehicleType=" + vehicleType + "}";
    }
}
