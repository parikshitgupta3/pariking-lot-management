package com.rapidstack.pariking_lot_management.infrastructure.persistence.entity;

import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA representation of a {@link com.rapidstack.pariking_lot_management.domain.model.Vehicle}.
 *
 * <p>The registration number is the primary key — the domain model's natural
 * identity maps directly, so no surrogate id is needed.
 */
@Entity
@Table(name = "vehicles")
public class VehicleEntity {

    @Id
    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    protected VehicleEntity() {
        // for JPA
    }

    public VehicleEntity(String registrationNumber, VehicleType vehicleType) {
        this.registrationNumber = registrationNumber;
        this.vehicleType = vehicleType;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
}
