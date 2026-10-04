package com.rapidstack.pariking_lot_management.domain.exception;

/**
 * Raised when a parking ticket's current state does not permit the requested
 * operation.
 *
 * <p>Base type for ticket-state violations. Extends
 * {@link IllegalStateException} so callers catching the standard exception
 * keep working. Specific conditions have their own subtypes (e.g.
 * {@link TicketAlreadyCompletedException}); ticket statuses introduced in
 * the future should follow the same pattern.
 */
public class InvalidTicketStateException extends IllegalStateException {

    public InvalidTicketStateException(String message) {
        super(message);
    }
}
