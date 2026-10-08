package com.rapidstack.pariking_lot_management.infrastructure.persistence.adapter;

import com.rapidstack.pariking_lot_management.application.port.ParkingSpotRepository;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.mapper.ParkingSpotMapper;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.repository.ParkingSpotJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * The JPA-backed implementation of the application's
 * {@link ParkingSpotRepository} port: a pessimistic row lock used to
 * confirm spot availability during allocation.
 */
@Repository
public class ParkingSpotRepositoryAdapter implements ParkingSpotRepository {

    private final ParkingSpotJpaRepository spots;

    public ParkingSpotRepositoryAdapter(ParkingSpotJpaRepository spots) {
        this.spots = spots;
    }

    @Override
    @Transactional
    public Optional<ParkingSpot> findByIdForUpdate(String spotId) {
        return spots.findByIdForUpdate(spotId)
                .map(ParkingSpotMapper::toDomain);
    }
}
