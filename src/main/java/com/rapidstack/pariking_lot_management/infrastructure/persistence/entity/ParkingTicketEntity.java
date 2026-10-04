package com.rapidstack.pariking_lot_management.infrastructure.persistence.entity;

import com.rapidstack.pariking_lot_management.domain.enums.TicketStatus;
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

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA representation of a {@link com.rapidstack.pariking_lot_management.domain.model.ParkingTicket}.
 *
 * <p>References the vehicle by its natural key (registration number) and the
 * spot by id. The vehicle and spot associations are eager because the
 * domain mapping happens outside the repository transaction (open-in-view
 * is disabled).
 */
@Entity
@Table(name = "parking_tickets")
public class ParkingTicketEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vehicle_registration_number", nullable = false,
            foreignKey = @ForeignKey(name = "fk_parking_tickets_vehicle"))
    private VehicleEntity vehicle;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "spot_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_parking_tickets_spot"))
    private ParkingSpotEntity spot;

    @Column(name = "entry_time", nullable = false)
    private Instant entryTime;

    @Column(name = "exit_time")
    private Instant exitTime;

    @Column(name = "fee", precision = 10, scale = 2)
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    protected ParkingTicketEntity() {
        // for JPA
    }

    public ParkingTicketEntity(String id, VehicleEntity vehicle, ParkingSpotEntity spot,
                                Instant entryTime, Instant exitTime, BigDecimal fee, TicketStatus status) {
        this.id = id;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.fee = fee;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public VehicleEntity getVehicle() {
        return vehicle;
    }

    public ParkingSpotEntity getSpot() {
        return spot;
    }

    public Instant getEntryTime() {
        return entryTime;
    }

    public Instant getExitTime() {
        return exitTime;
    }

    public void setExitTime(Instant exitTime) {
        this.exitTime = exitTime;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
