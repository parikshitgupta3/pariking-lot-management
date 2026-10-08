package com.rapidstack.pariking_lot_management.infrastructure.persistence.repository;

import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingSpotEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data repository for {@link ParkingSpotEntity}.
 */
@Repository
public interface ParkingSpotJpaRepository extends JpaRepository<ParkingSpotEntity, String> {

    /**
     * Loads a spot with a pessimistic row lock — emits
     * {@code SELECT ... FOR UPDATE} on PostgreSQL. Must run inside a
     * transaction; the lock is held until it commits or rolls back.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ParkingSpotEntity s where s.id = :id")
    Optional<ParkingSpotEntity> findByIdForUpdate(@Param("id") String id);
}
