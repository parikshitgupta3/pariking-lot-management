package com.rapidstack.pariking_lot_management.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA representation of a {@link com.rapidstack.pariking_lot_management.domain.model.ParkingFloor}.
 *
 * <p>The (lot, floorNumber) unique constraint mirrors the domain's
 * no-duplicate-floor-numbers invariant at the database level.
 */
@Entity
@Table(name = "parking_floor",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_parking_floor_lot_floor_number",
                columnNames = {"lot_id", "floor_number"}))
public class ParkingFloorEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "floor_number", nullable = false)
    private int floorNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_parking_floor_lot"))
    private ParkingLotEntity lot;

    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("spotNumber ASC")
    private List<ParkingSpotEntity> spots = new ArrayList<>();

    protected ParkingFloorEntity() {
        // for JPA
    }

    public ParkingFloorEntity(String id, int floorNumber) {
        this.id = id;
        this.floorNumber = floorNumber;
    }

    public void addSpot(ParkingSpotEntity spot) {
        spots.add(spot);
        spot.setFloor(this);
    }

    public String getId() {
        return id;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public ParkingLotEntity getLot() {
        return lot;
    }

    public void setLot(ParkingLotEntity lot) {
        this.lot = lot;
    }

    public List<ParkingSpotEntity> getSpots() {
        return spots;
    }
}
