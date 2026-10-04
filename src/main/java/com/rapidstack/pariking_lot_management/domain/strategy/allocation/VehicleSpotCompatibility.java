package com.rapidstack.pariking_lot_management.domain.strategy.allocation;

import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The domain policy that determines which spot types fit which vehicle
 * types.
 *
 * <p>A spot supports its own size class and anything smaller: a BIKE spot
 * fits bikes only, a COMPACT spot fits bikes and cars, and a LARGE spot fits
 * bikes, cars, and trucks.
 */
public final class VehicleSpotCompatibility {

    private static final Map<VehicleType, Set<SpotType>> SUPPORTED_SPOT_TYPES = new EnumMap<>(VehicleType.class);

    static {
        SUPPORTED_SPOT_TYPES.put(VehicleType.BIKE, Set.of(SpotType.BIKE, SpotType.COMPACT, SpotType.LARGE));
        SUPPORTED_SPOT_TYPES.put(VehicleType.CAR, Set.of(SpotType.COMPACT, SpotType.LARGE));
        SUPPORTED_SPOT_TYPES.put(VehicleType.TRUCK, Set.of(SpotType.LARGE));
    }

    private VehicleSpotCompatibility() {
    }

    /**
     * @return whether a vehicle of the given type can park on a spot of the
     *         given type
     */
    public static boolean supports(SpotType spotType, VehicleType vehicleType) {
        Objects.requireNonNull(spotType, "spotType must not be null");
        Objects.requireNonNull(vehicleType, "vehicleType must not be null");
        return supportedSpotTypes(vehicleType).contains(spotType);
    }

    /**
     * @return the spot types a vehicle of the given type can park on
     */
    public static Set<SpotType> supportedSpotTypes(VehicleType vehicleType) {
        Objects.requireNonNull(vehicleType, "vehicleType must not be null");
        Set<SpotType> supported = SUPPORTED_SPOT_TYPES.get(vehicleType);
        if (supported == null) {
            // Fail loudly if a new VehicleType is added without a mapping.
            throw new IllegalStateException("No spot-type mapping defined for vehicle type: " + vehicleType);
        }
        return supported;
    }
}
