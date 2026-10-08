package com.rapidstack.pariking_lot_management.application.port;

import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;

import java.util.Optional;

/**
 * The spot persistence port. Exists for one purpose: pessimistic row
 * locking during spot allocation. The allocation strategy selects a
 * candidate from an in-memory snapshot; before committing to that spot, the
 * entry use case must confirm the row is still available under a lock
 * ({@code SELECT ... FOR UPDATE} on PostgreSQL), so two concurrent entries
 * can never occupy the same spot.
 */
public interface ParkingSpotRepository {

    /**
     * Loads the spot and pessimistically locks its row for the rest of the
     * current transaction. The returned domain object reflects the current
     * database state, which may differ from the in-memory snapshot the
     * caller allocated from.
     *
     * @return the spot with the given id, or empty if it no longer exists
     */
    Optional<ParkingSpot> findByIdForUpdate(String spotId);
}
