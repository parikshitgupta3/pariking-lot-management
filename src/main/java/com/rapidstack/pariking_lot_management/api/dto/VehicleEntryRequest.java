package com.rapidstack.pariking_lot_management.api.dto;

import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for admitting a vehicle to a lot.
 */
public record VehicleEntryRequest(
        @NotBlank(message = "registrationNumber must not be blank") String registrationNumber,
        @NotNull(message = "vehicleType must not be null") VehicleType vehicleType) {

    public Vehicle toDomain() {
        return new Vehicle(registrationNumber, vehicleType);
    }
}
