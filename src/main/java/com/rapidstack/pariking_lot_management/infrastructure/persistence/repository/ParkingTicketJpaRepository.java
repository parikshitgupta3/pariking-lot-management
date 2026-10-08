package com.rapidstack.pariking_lot_management.infrastructure.persistence.repository;

import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingTicketEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link ParkingTicketEntity}.
 */
@Repository
public interface ParkingTicketJpaRepository extends JpaRepository<ParkingTicketEntity, String> {

    /**
     * Loads a ticket with a pessimistic row lock — emits
     * {@code SELECT ... FOR UPDATE} on PostgreSQL. Must run inside a
     * transaction; the lock is held until it commits or rolls back.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from ParkingTicketEntity t where t.id = :id")
    Optional<ParkingTicketEntity> findByIdForUpdate(@Param("id") String id);

    /**
     * Every ACTIVE ticket whose spot belongs to the given lot, via the
     * ticket → spot → floor → lot association.
     */
    @Query("select t from ParkingTicketEntity t "
            + "where t.status = com.rapidstack.pariking_lot_management.domain.enums.TicketStatus.ACTIVE "
            + "and t.spot.floor.lot.id = :lotId")
    List<ParkingTicketEntity> findActiveByLotId(@Param("lotId") String lotId);
}
