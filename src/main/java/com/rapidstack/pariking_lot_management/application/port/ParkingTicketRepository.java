package com.rapidstack.pariking_lot_management.application.port;

import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;

import java.util.List;
import java.util.Optional;

/**
 * The ticket persistence port: the use-case layer's statement of what it
 * needs from infrastructure to run the exit workflow.
 *
 * <p>Owned by the application layer because only use cases consume it — the
 * domain model itself never reads or writes tickets, so the domain carries
 * no persistence vocabulary. The infrastructure layer provides the JPA-backed
 * adapter as a Spring bean. Deliberately framework-free so it can be
 * satisfied by anything from a database adapter to an in-memory fake in
 * tests.
 */
public interface ParkingTicketRepository {

    /**
     * @return the ticket with the given id, or empty if none exists
     */
    Optional<ParkingTicket> findById(String ticketId);

    /**
     * Loads the ticket and pessimistically locks its row for the rest of the
     * current transaction ({@code SELECT ... FOR UPDATE} on PostgreSQL).
     * Use inside a transaction when the ticket is about to be mutated: a
     * concurrent writer of the same ticket blocks until this transaction
     * commits or rolls back, preventing lost updates such as a double
     * checkout.
     *
     * @return the ticket with the given id, or empty if none exists
     */
    Optional<ParkingTicket> findByIdForUpdate(String ticketId);

    /**
     * Persists the current state of the given ticket.
     */
    void save(ParkingTicket ticket);

    /**
     * @return every ACTIVE ticket whose spot belongs to the given lot
     */
    List<ParkingTicket> findActiveByLotId(String lotId);

    /**
     * @return every ACTIVE ticket across all lots
     */
    List<ParkingTicket> findAllActive();
}
