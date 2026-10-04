package com.rapidstack.pariking_lot_management.infrastructure.persistence.repository;

import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingGateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link ParkingGateEntity}.
 */
@Repository
public interface ParkingGateJpaRepository extends JpaRepository<ParkingGateEntity, String> {
}
