package com.rapidstack.pariking_lot_management.infrastructure.persistence.entity;

import com.rapidstack.pariking_lot_management.domain.enums.GateType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * JPA representation of a {@link com.rapidstack.pariking_lot_management.domain.model.ParkingGate}.
 *
 * <p>The owning lot association exists only at the persistence level — the
 * domain model has no gate→lot reference yet — because gate numbers are
 * only meaningful per lot (hence the (lot, gateNumber) unique constraint).
 * The association is simply not mapped back into the domain model until the
 * domain grows one.
 */
@Entity
@Table(name = "parking_gate",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_parking_gate_lot_gate_number",
                columnNames = {"lot_id", "gate_number"}))
public class ParkingGateEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "gate_number", nullable = false)
    private int gateNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gate_type", nullable = false)
    private GateType gateType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_parking_gate_lot"))
    private ParkingLotEntity lot;

    protected ParkingGateEntity() {
        // for JPA
    }

    public ParkingGateEntity(String id, int gateNumber, GateType gateType) {
        this.id = id;
        this.gateNumber = gateNumber;
        this.gateType = gateType;
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

    public ParkingLotEntity getLot() {
        return lot;
    }

    public void setLot(ParkingLotEntity lot) {
        this.lot = lot;
    }
}
