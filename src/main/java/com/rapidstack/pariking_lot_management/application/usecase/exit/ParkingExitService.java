package com.rapidstack.pariking_lot_management.application.usecase.exit;

import com.rapidstack.pariking_lot_management.application.exception.TicketNotFoundException;
import com.rapidstack.pariking_lot_management.application.port.ParkingTicketRepository;
import com.rapidstack.pariking_lot_management.domain.enums.TicketStatus;
import com.rapidstack.pariking_lot_management.domain.exception.TicketAlreadyCompletedException;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import com.rapidstack.pariking_lot_management.domain.strategy.pricing.ParkingFeeStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Orchestrates the vehicle exit workflow atomically, in one transaction:
 * ticket completion, fee recording, and spot release either all commit or
 * none of them do.
 *
 * <p>The ticket is loaded with a pessimistic row lock
 * ({@link ParkingTicketRepository#findByIdForUpdate}), so a concurrent
 * checkout of the same ticket blocks until this transaction commits and
 * then sees the ticket COMPLETED — a double exit can never succeed twice.
 * This service owns no business rules: pricing lives in the fee strategy,
 * ticket invariants and spot transitions in the domain model. Within the
 * transaction the ticket is closed before the spot is released (pay, then
 * the gate opens).
 */
@Service
public class ParkingExitService {

    private final ParkingTicketRepository ticketRepository;
    private final ParkingFeeStrategy feeStrategy;
    private final Clock clock;

    public ParkingExitService(ParkingTicketRepository ticketRepository,
                              ParkingFeeStrategy feeStrategy,
                              Clock clock) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository, "ticketRepository must not be null");
        this.feeStrategy = Objects.requireNonNull(feeStrategy, "feeStrategy must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * Checks out the vehicle holding the given ticket.
     *
     * @throws TicketNotFoundException         if no ticket exists for the id
     * @throws TicketAlreadyCompletedException if the ticket was already
     *                                         checked out (including by a
     *                                         concurrent request)
     */
    @Transactional
    public ParkingTicket checkout(String ticketId) {
        Objects.requireNonNull(ticketId, "ticketId must not be null");
        if (ticketId.isBlank()) {
            throw new IllegalArgumentException("ticketId must not be blank");
        }

        ParkingTicket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("No parking ticket found for id '" + ticketId + "'"));
        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new TicketAlreadyCompletedException(
                    "Ticket " + ticketId + " is already completed (exit time: " + ticket.getExitTime() + ")");
        }

        Instant exitTime = clock.instant();
        Duration parkingDuration = Duration.between(ticket.getEntryTime(), exitTime);
        BigDecimal fee = feeStrategy.calculateFee(ticket.getVehicle().getVehicleType(), parkingDuration);

        ticket.close(exitTime, fee);
        ticket.getSpot().release();
        ticketRepository.save(ticket);
        return ticket;
    }

    /**
     * Looks up a ticket by id, active or completed.
     *
     * @throws TicketNotFoundException if no ticket exists for the id
     */
    public ParkingTicket getTicket(String ticketId) {
        Objects.requireNonNull(ticketId, "ticketId must not be null");
        if (ticketId.isBlank()) {
            throw new IllegalArgumentException("ticketId must not be blank");
        }
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("No parking ticket found for id '" + ticketId + "'"));
    }
}
