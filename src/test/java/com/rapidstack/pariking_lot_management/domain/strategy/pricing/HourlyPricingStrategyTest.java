package com.rapidstack.pariking_lot_management.domain.strategy.pricing;

import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HourlyPricingStrategyTest {

    private final HourlyPricingStrategy strategy = new HourlyPricingStrategy(new BigDecimal("10.00"));

    private void assertFee(String expected, Duration duration) {
        BigDecimal fee = strategy.calculateFee(VehicleType.CAR, duration);
        assertEquals(0, new BigDecimal(expected).compareTo(fee),
                "expected " + expected + " for " + duration + " but got " + fee);
    }

    @Test
    void zeroLengthStayCostsNothing() {
        assertFee("0.00", Duration.ZERO);
    }

    @Test
    void partialHourIsBilledAsOneFullHour() {
        assertFee("10.00", Duration.ofSeconds(1));
        assertFee("10.00", Duration.ofMinutes(59));
    }

    @Test
    void exactHourIsBilledAsOneHour() {
        assertFee("10.00", Duration.ofMinutes(60));
    }

    @Test
    void hourAndAMinuteIsBilledAsTwoHours() {
        assertFee("20.00", Duration.ofMinutes(61));
    }

    @Test
    void multiHourStayBillsEachStartedHour() {
        assertFee("30.00", Duration.ofMinutes(150)); // 2.5h -> 3 started hours
    }

    @Test
    void rateIsTheSameForEveryVehicleType() {
        BigDecimal carFee = strategy.calculateFee(VehicleType.CAR, Duration.ofMinutes(90));
        BigDecimal truckFee = strategy.calculateFee(VehicleType.TRUCK, Duration.ofMinutes(90));
        BigDecimal bikeFee = strategy.calculateFee(VehicleType.BIKE, Duration.ofMinutes(90));

        assertEquals(0, carFee.compareTo(truckFee));
        assertEquals(0, carFee.compareTo(bikeFee));
    }

    @Test
    void zeroRateMeansFreeParking() {
        HourlyPricingStrategy freeStrategy = new HourlyPricingStrategy(BigDecimal.ZERO);

        BigDecimal fee = freeStrategy.calculateFee(VehicleType.CAR, Duration.ofHours(5));

        assertEquals(0, BigDecimal.ZERO.compareTo(fee));
    }

    @Test
    void negativeDurationIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> strategy.calculateFee(VehicleType.CAR, Duration.ofMinutes(-1)));
    }

    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class,
                () -> strategy.calculateFee(null, Duration.ofHours(1)));
        assertThrows(NullPointerException.class,
                () -> strategy.calculateFee(VehicleType.CAR, null));
    }

    @Test
    void negativeRateIsRejectedAtConstruction() {
        assertThrows(IllegalArgumentException.class,
                () -> new HourlyPricingStrategy(new BigDecimal("-1.00")));
        assertThrows(NullPointerException.class,
                () -> new HourlyPricingStrategy(null));
    }

    @Test
    void feeIsNeverNegative() {
        assertTrue(strategy.calculateFee(VehicleType.CAR, Duration.ZERO).signum() >= 0);
    }
}
