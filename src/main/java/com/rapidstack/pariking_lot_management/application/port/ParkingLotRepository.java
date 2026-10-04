package com.rapidstack.pariking_lot_management.application.port;

import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;

import java.util.List;
import java.util.Optional;

/**
 * The parking-lot persistence port: the use-case layer's statement of what
 * it needs from infrastructure to manage lots. Owned by the application
 * layer for the same reason as {@link ParkingTicketRepository} — only use
 * cases consume it. The infrastructure layer provides the JPA-backed
 * adapter.
 */
public interface ParkingLotRepository {

    /**
     * Persists the lot aggregate (floors and spots by cascade) and returns
     * the persisted state.
     */
    ParkingLot save(ParkingLot lot);

    /**
     * @return the lot with the given id, or empty if none exists
     */
    Optional<ParkingLot> findById(String id);

    /**
     * @return all persisted lots
     */
    List<ParkingLot> findAll();
}
