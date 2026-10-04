package com.rapidstack.pariking_lot_management.infrastructure.persistence.repository;

import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link VehicleEntity}. The id is the
 * registration number (the domain's natural identity).
 */
@Repository
public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, String> {
}
