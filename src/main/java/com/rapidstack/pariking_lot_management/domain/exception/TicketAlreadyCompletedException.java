package com.rapidstack.pariking_lot_management.domain.exception;

/**
 * Raised when an operation requires an active ticket but the ticket has
 * already been completed — a duplicate or replayed exit, or closing a ticket
 * twice.
 */
public class TicketAlreadyCompletedException extends InvalidTicketStateException {

    public TicketAlreadyCompletedException(String message) {
        super(message);
    }
}
