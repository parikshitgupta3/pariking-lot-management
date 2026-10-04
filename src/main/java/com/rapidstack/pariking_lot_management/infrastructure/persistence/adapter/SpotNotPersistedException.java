package com.rapidstack.pariking_lot_management.infrastructure.persistence.adapter;

/**
 * Thrown when a ticket is saved for a spot that has never been persisted —
 * spots belong to a stored lot aggregate, so a ticket for an unknown spot
 * would dangle. A dedicated type (rather than a plain
 * {@code IllegalStateException}) so Spring's repository exception
 * translation does not rewrap it on the way out of the transactional
 * adapter.
 */
public class SpotNotPersistedException extends RuntimeException {

    public SpotNotPersistedException(String message) {
        super(message);
    }
}
