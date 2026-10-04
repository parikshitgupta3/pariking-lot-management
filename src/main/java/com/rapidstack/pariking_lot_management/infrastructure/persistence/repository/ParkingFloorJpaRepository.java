package com.rapidstack.pariking_lot_management.infrastructure.persistence.repository;

import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingFloorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link ParkingFloorEntity}.
 */
@Repository
public interface ParkingFloorJpaRepository extends JpaRepository<ParkingFloorEntity, String> {
}
