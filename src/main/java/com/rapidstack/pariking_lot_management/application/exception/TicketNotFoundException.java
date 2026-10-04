package com.rapidstack.pariking_lot_management.application.exception;

/**
 * Thrown when checkout is attempted for a ticket id that no ticket exists
 * for — e.g. a typoed or fabricated id at the exit gate. A lookup failure
 * rather than a domain-rule violation, hence it lives with the application
 * layer.
 */
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String message) {
        super(message);
    }
}
