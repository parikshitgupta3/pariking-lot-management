package com.rapidstack.pariking_lot_management.domain.strategy.pricing;

import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * A pricing policy computing the fee for one parking stay.
 *
 * <p>Implementations are pure functions of the vehicle type and the parking
 * duration — they hold no state and know nothing about the exit workflow
 * that invokes them, so pricing can vary independently of orchestration.
 */
public interface ParkingFeeStrategy {

    /**
     * Computes the fee for a stay of the given duration.
     *
     * @param vehicleType     the type of vehicle that was parked; part of the
     *                        contract so future implementations can price by
     *                        type
     * @param parkingDuration the elapsed time between entry and exit;
     *                        never negative
     * @return the fee owed for the stay, never negative
     */
    BigDecimal calculateFee(VehicleType vehicleType, Duration parkingDuration);
}
