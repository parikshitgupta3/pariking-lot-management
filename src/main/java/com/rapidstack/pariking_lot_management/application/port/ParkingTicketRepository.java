package com.rapidstack.pariking_lot_management.application.port;

import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;

import java.util.Optional;

/**
 * The ticket persistence port: the use-case layer's statement of what it
 * needs from infrastructure to run the exit workflow.
 *
 * <p>Owned by the application layer because only use cases consume it — the
 * domain model itself never reads or writes tickets, so the domain carries
 * no persistence vocabulary. The infrastructure layer (once persistence is
 * introduced) provides the adapter, e.g. a JPA-backed implementation
 * registered as a bean in {@code ApplicationConfig}. Deliberately
 * framework-free so it can be satisfied by anything from a database adapter
 * to an in-memory fake in tests.
 */
public interface ParkingTicketRepository {

    /**
     * @return the ticket with the given id, or empty if none exists
     */
    Optional<ParkingTicket> findById(String ticketId);

    /**
     * Persists the current state of the given ticket.
     */
    void save(ParkingTicket ticket);
}
