package com.rapidstack.pariking_lot_management.config;

import com.rapidstack.pariking_lot_management.domain.strategy.allocation.FirstAvailableSpotStrategy;
import com.rapidstack.pariking_lot_management.domain.strategy.allocation.SpotAllocationStrategy;
import com.rapidstack.pariking_lot_management.domain.strategy.pricing.HourlyPricingStrategy;
import com.rapidstack.pariking_lot_management.domain.strategy.pricing.ParkingFeeStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Clock;

/**
 * Wires the application-layer collaborators. The domain strategies stay
 * framework-free; they are exposed as beans here instead, so the domain
 * package carries no Spring imports. The ticket repository port is satisfied
 * by the {@code ParkingTicketRepositoryAdapter} in the infrastructure layer
 * (picked up by component scan via its {@code @Repository} annotation).
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public SpotAllocationStrategy spotAllocationStrategy() {
        return new FirstAvailableSpotStrategy();
    }

    /**
     * Flat 10.00 per started hour, the same for every vehicle type.
     */
    @Bean
    public ParkingFeeStrategy parkingFeeStrategy() {
        return new HourlyPricingStrategy(new BigDecimal("10.00"));
    }
}
