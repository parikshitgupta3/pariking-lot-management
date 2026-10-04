package com.rapidstack.pariking_lot_management.infrastructure.persistence.repository;

import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingLotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link ParkingLotEntity}.
 */
@Repository
public interface ParkingLotJpaRepository extends JpaRepository<ParkingLotEntity, String> {
}
