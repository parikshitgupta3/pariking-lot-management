package com.rapidstack.pariking_lot_management.domain.model;

import com.rapidstack.pariking_lot_management.domain.enums.TicketStatus;
import com.rapidstack.pariking_lot_management.domain.exception.TicketAlreadyCompletedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A ticket recording one vehicle's stay on one spot.
 *
 * <p>Created ACTIVE with an entry time, no exit time, and no fee. The ticket
 * is closed exactly once via {@link #close(Instant, BigDecimal)}, which pairs
 * the exit time, the fee, and the COMPLETED status so the three can never
 * disagree. The fee is computed by an external pricing policy (a
 * {@code ParkingFeeStrategy}) — the ticket only records it. The all-args
 * constructor enforces the same consistency for rehydrating persisted
 * tickets.
 */
public final class ParkingTicket {

    private final String id;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final Instant entryTime;
    private Instant exitTime;
    private BigDecimal fee;
    private TicketStatus status;

    public ParkingTicket(String id, Vehicle vehicle, ParkingSpot spot, Instant entryTime) {
        this(id, vehicle, spot, entryTime, null, null, TicketStatus.ACTIVE);
    }

    public ParkingTicket(String id, Vehicle vehicle, ParkingSpot spot, Instant entryTime,
                         Instant exitTime, BigDecimal fee, TicketStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle must not be null");
        this.spot = Objects.requireNonNull(spot, "spot must not be null");
        this.entryTime = Objects.requireNonNull(entryTime, "entryTime must not be null");
        Objects.requireNonNull(status, "status must not be null");
        if (status == TicketStatus.COMPLETED) {
            Objects.requireNonNull(exitTime, "exitTime must not be null when status is COMPLETED");
            if (exitTime.isBefore(entryTime)) {
                throw new IllegalArgumentException("exitTime must not be before entryTime");
            }
            Objects.requireNonNull(fee, "fee must not be null when status is COMPLETED");
            if (fee.signum() < 0) {
                throw new IllegalArgumentException("fee must not be negative");
            }
        } else {
            if (exitTime != null) {
                throw new IllegalArgumentException("exitTime must be null while status is ACTIVE");
            }
            if (fee != null) {
                throw new IllegalArgumentException("fee must be null while status is ACTIVE");
            }
        }
        this.id = id;
        this.exitTime = exitTime;
        this.fee = fee;
        this.status = status;
    }

    /**
     * Closes the ticket: records the exit time and the fee charged for the
     * stay, and marks it COMPLETED.
     *
     * @throws IllegalArgumentException      if the exit time is before the entry
     *                                       time or the fee is negative
     * @throws TicketAlreadyCompletedException if the ticket is already
     *                                       completed
     */
    public void close(Instant exitTime, BigDecimal fee) {
        Objects.requireNonNull(exitTime, "exitTime must not be null");
        Objects.requireNonNull(fee, "fee must not be null");
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("exitTime must not be before entryTime");
        }
        if (fee.signum() < 0) {
            throw new IllegalArgumentException("fee must not be negative");
        }
        if (status == TicketStatus.COMPLETED) {
            throw new TicketAlreadyCompletedException("ticket " + id + " is already completed");
        }
        this.exitTime = exitTime;
        this.fee = fee;
        this.status = TicketStatus.COMPLETED;
    }

    public String getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getSpot() {
        return spot;
    }

    public Instant getEntryTime() {
        return entryTime;
    }

    /**
     * The exit time, or {@code null} while the ticket is still active.
     */
    public Instant getExitTime() {
        return exitTime;
    }

    /**
     * The fee charged for the stay, or {@code null} while the ticket is
     * still active.
     */
    public BigDecimal getFee() {
        return fee;
    }

    public TicketStatus getStatus() {
        return status;
    }

    /**
     * Identity is the id; two tickets with the same id are the same ticket
     * regardless of their current status, exit time, or fee.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParkingTicket that)) {
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
        return "ParkingTicket{id='" + id + "', vehicle=" + vehicle.getRegistrationNumber()
                + ", spot=" + spot.getSpotNumber() + ", entryTime=" + entryTime
                + ", exitTime=" + exitTime + ", fee=" + fee + ", status=" + status + "}";
    }
}
