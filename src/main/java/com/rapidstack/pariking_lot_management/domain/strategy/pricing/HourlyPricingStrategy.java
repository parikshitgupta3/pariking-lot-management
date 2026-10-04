package com.rapidstack.pariking_lot_management.domain.strategy.pricing;

import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

/**
 * Basic hourly pricing: a single flat rate per started hour, the same for
 * every vehicle type. Each started hour is billed in full (a 61-minute stay
 * bills as two hours) and a zero-length stay costs nothing.
 */
public final class HourlyPricingStrategy implements ParkingFeeStrategy {

    private static final long SECONDS_PER_HOUR = Duration.ofHours(1).toSeconds();

    private final BigDecimal hourlyRate;

    /**
     * @param hourlyRate the rate per started hour; zero allowed (free
     *                   parking), negative not
     */
    public HourlyPricingStrategy(BigDecimal hourlyRate) {
        Objects.requireNonNull(hourlyRate, "hourlyRate must not be null");
        if (hourlyRate.signum() < 0) {
            throw new IllegalArgumentException("hourlyRate must not be negative");
        }
        this.hourlyRate = hourlyRate;
    }

    @Override
    public BigDecimal calculateFee(VehicleType vehicleType, Duration parkingDuration) {
        Objects.requireNonNull(vehicleType, "vehicleType must not be null");
        Objects.requireNonNull(parkingDuration, "parkingDuration must not be null");
        if (parkingDuration.isNegative()) {
            throw new IllegalArgumentException("parkingDuration must not be negative");
        }
        long billedHours = Math.ceilDiv(parkingDuration.toSeconds(), SECONDS_PER_HOUR);
        return hourlyRate.multiply(BigDecimal.valueOf(billedHours));
    }
}
