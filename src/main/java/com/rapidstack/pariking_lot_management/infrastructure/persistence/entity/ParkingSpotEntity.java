package com.rapidstack.pariking_lot_management.infrastructure.persistence.entity;

import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
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
 * JPA representation of a {@link com.rapidstack.pariking_lot_management.domain.model.ParkingSpot}.
 *
 * <p>The (floor, spotNumber) unique constraint mirrors the domain's
 * no-duplicate-spot-numbers-per-floor invariant at the database level — the
 * requirement's primary data-integrity rule.
 */
@Entity
@Table(name = "parking_spot",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_parking_spot_floor_spot_number",
                columnNames = {"floor_id", "spot_number"}))
public class ParkingSpotEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "spot_number", nullable = false)
    private String spotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "spot_type", nullable = false)
    private SpotType spotType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SpotStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "floor_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_parking_spot_floor"))
    private ParkingFloorEntity floor;

    protected ParkingSpotEntity() {
        // for JPA
    }

    public ParkingSpotEntity(String id, String spotNumber, SpotType spotType, SpotStatus status) {
        this.id = id;
        this.spotNumber = spotNumber;
        this.spotType = spotType;
        this.status = status;
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
        this.status = status;
    }

    public ParkingFloorEntity getFloor() {
        return floor;
    }

    public void setFloor(ParkingFloorEntity floor) {
        this.floor = floor;
    }
}
