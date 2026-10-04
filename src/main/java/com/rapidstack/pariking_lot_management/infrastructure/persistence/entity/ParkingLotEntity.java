package com.rapidstack.pariking_lot_management.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA representation of a {@link com.rapidstack.pariking_lot_management.domain.model.ParkingLot}.
 *
 * <p>Aggregate root of the lot/floor/spot hierarchy: floors (and their
 * spots, transitively) are persisted by cascade. Collections are eager
 * because domain mapping happens outside the repository transaction
 * (open-in-view is disabled).
 */
@Entity
@Table(name = "parking_lots")
public class ParkingLotEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("floorNumber ASC")
    private List<ParkingFloorEntity> floors = new ArrayList<>();

    protected ParkingLotEntity() {
        // for JPA
    }

    public ParkingLotEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addFloor(ParkingFloorEntity floor) {
        floors.add(floor);
        floor.setLot(this);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<ParkingFloorEntity> getFloors() {
        return floors;
    }
}
