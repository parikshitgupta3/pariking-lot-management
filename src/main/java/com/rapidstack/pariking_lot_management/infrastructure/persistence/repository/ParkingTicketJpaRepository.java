package com.rapidstack.pariking_lot_management.infrastructure.persistence.repository;

import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link ParkingTicketEntity}.
 */
@Repository
public interface ParkingTicketJpaRepository extends JpaRepository<ParkingTicketEntity, String> {
}
