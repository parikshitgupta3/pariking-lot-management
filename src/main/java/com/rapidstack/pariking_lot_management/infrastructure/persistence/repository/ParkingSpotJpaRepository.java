package com.rapidstack.pariking_lot_management.infrastructure.persistence.repository;

import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingSpotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link ParkingSpotEntity}.
 */
@Repository
public interface ParkingSpotJpaRepository extends JpaRepository<ParkingSpotEntity, String> {
}
